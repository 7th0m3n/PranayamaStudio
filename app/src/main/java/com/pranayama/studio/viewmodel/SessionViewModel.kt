package com.pranayama.studio.viewmodel

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pranayama.studio.data.StatsRepository
import com.pranayama.studio.model.BreathPattern
import com.pranayama.studio.model.BreathPhase
import com.pranayama.studio.model.PranayamaPatterns
import com.pranayama.studio.model.SessionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current state of a breathing session.
 */
data class SessionState(
    // Pattern info
    val pattern: BreathPattern = PranayamaPatterns.boxBreathing,
    
    // Session state
    val isPlaying: Boolean = false,
    val isComplete: Boolean = false,
    
    // Current phase
    val currentPhase: BreathPhase = BreathPhase.INHALE,
    val phaseSecondsRemaining: Int = 4,
    val phaseTotalSeconds: Int = 4,
    
    // Progress tracking
    val currentCycle: Int = 1,
    val totalCycles: Int = 10,
    val elapsedSeconds: Int = 0,
    val totalSessionSeconds: Int = 300, // 5 minutes default
    
    // Animation progress (0.0 to 1.0 for smooth animations)
    val phaseProgress: Float = 0f,
    val breathProgress: Float = 0f // 0 = empty lungs, 1 = full lungs
)

/**
 * ViewModel for managing breathing session state and timing.
 * 
 * ===== STATE MACHINE FOR BREATHING PHASES =====
 * 
 * The breathing cycle follows this pattern:
 * 
 *   INHALE → HOLD_AFTER_INHALE → EXHALE → HOLD_AFTER_EXHALE → (repeat)
 *                                                              ↓
 *                                                         back to INHALE
 * 
 * Some patterns may skip certain hold phases (e.g., 4-7-8 has no hold after exhale).
 * When a phase has 0 duration, it's automatically skipped.
 * 
 * ===== TIMING APPROACH =====
 * 
 * We use a simple approach with coroutines:
 * 1. A main timer loop runs every 100ms for smooth animation
 * 2. Every second, we decrement the phase countdown
 * 3. When countdown hits 0, we transition to the next phase
 * 4. We track both per-phase progress and overall breath "fullness"
 * 
 * ===== WHERE TO ADD SOUND CUES =====
 * 
 * To add audio cues, modify the onPhaseChange() function below.
 * You could:
 * - Play a soft chime on phase change
 * - Use Text-to-Speech to announce "Inhale", "Hold", etc.
 * - Play ambient breathing guide sounds
 */
class SessionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()
    
    private val statsRepository = StatsRepository(application)
    val statsFlow = statsRepository.statsFlow
    
    // Vibrator for haptic feedback
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(VibratorManager::class.java)
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Vibrator::class.java)
    }
    
    // Timer job
    private var timerJob: Job? = null
    
    // Session start time for stats
    private var sessionStartTime: Long = 0
    
    /**
     * Initialize a session with the given pattern.
     */
    fun initSession(patternId: String) {
        val pattern = PranayamaPatterns.findById(patternId) ?: PranayamaPatterns.boxBreathing
        
        val totalSeconds = if (pattern.sessionType == SessionType.TIMED) {
            pattern.defaultMinutes * 60
        } else {
            pattern.defaultCycles * pattern.cycleDurationSeconds
        }
        
        val totalCycles = if (pattern.sessionType == SessionType.CYCLES) {
            pattern.defaultCycles
        } else {
            // Calculate approximate cycles for time-based
            totalSeconds / pattern.cycleDurationSeconds
        }
        
        _state.value = SessionState(
            pattern = pattern,
            isPlaying = false,
            isComplete = false,
            currentPhase = BreathPhase.INHALE,
            phaseSecondsRemaining = pattern.inhaleSeconds,
            phaseTotalSeconds = pattern.inhaleSeconds,
            currentCycle = 1,
            totalCycles = totalCycles,
            elapsedSeconds = 0,
            totalSessionSeconds = totalSeconds,
            phaseProgress = 0f,
            breathProgress = 0f
        )
    }
    
    /**
     * Start or resume the breathing session.
     */
    fun play() {
        if (_state.value.isComplete) return
        
        _state.value = _state.value.copy(isPlaying = true)
        
        if (sessionStartTime == 0L) {
            sessionStartTime = System.currentTimeMillis()
        }
        
        startTimer()
    }
    
    /**
     * Pause the session.
     */
    fun pause() {
        _state.value = _state.value.copy(isPlaying = false)
        timerJob?.cancel()
    }
    
    /**
     * Toggle play/pause state.
     */
    fun togglePlayPause() {
        if (_state.value.isPlaying) pause() else play()
    }
    
    /**
     * End the current session early.
     */
    fun endSession() {
        timerJob?.cancel()
        recordSessionStats()
        _state.value = _state.value.copy(
            isPlaying = false,
            isComplete = true
        )
    }
    
    /**
     * Reset for a new session with the same pattern.
     */
    fun resetSession() {
        timerJob?.cancel()
        sessionStartTime = 0
        initSession(_state.value.pattern.id)
    }
    
    /**
     * Main timer loop - runs every 100ms for smooth animations.
     */
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var tickCount = 0
            
            while (_state.value.isPlaying && !_state.value.isComplete) {
                delay(100) // 100ms tick for smooth animation
                tickCount++
                
                val current = _state.value
                val pattern = current.pattern
                
                // Update phase progress (0 to 1)
                val phaseElapsed = current.phaseTotalSeconds - current.phaseSecondsRemaining + (tickCount % 10) / 10f
                val newPhaseProgress = (phaseElapsed / current.phaseTotalSeconds).coerceIn(0f, 1f)
                
                // Calculate breath fullness based on phase
                val newBreathProgress = calculateBreathProgress(current.currentPhase, newPhaseProgress, current.breathProgress)
                
                // Every second (10 ticks), update countdown
                if (tickCount % 10 == 0) {
                    val newSecondsRemaining = current.phaseSecondsRemaining - 1
                    val newElapsed = current.elapsedSeconds + 1
                    
                    if (newSecondsRemaining <= 0) {
                        // Phase complete - transition to next
                        transitionToNextPhase(newElapsed)
                    } else {
                        // Update countdown
                        _state.value = current.copy(
                            phaseSecondsRemaining = newSecondsRemaining,
                            elapsedSeconds = newElapsed,
                            phaseProgress = 0f, // Reset for new second
                            breathProgress = newBreathProgress
                        )
                    }
                    tickCount = 0
                } else {
                    // Sub-second animation update
                    _state.value = current.copy(
                        phaseProgress = newPhaseProgress,
                        breathProgress = newBreathProgress
                    )
                }
            }
        }
    }
    
    /**
     * Calculate the current "breath fullness" based on phase and progress.
     * Returns 0.0 (empty lungs) to 1.0 (full lungs)
     */
    private fun calculateBreathProgress(phase: BreathPhase, phaseProgress: Float, currentProgress: Float): Float {
        return when (phase) {
            BreathPhase.INHALE -> phaseProgress // Filling up
            BreathPhase.HOLD_AFTER_INHALE -> 1f // Lungs full
            BreathPhase.EXHALE -> 1f - phaseProgress // Emptying
            BreathPhase.HOLD_AFTER_EXHALE -> 0f // Lungs empty
        }
    }
    
    /**
     * Transition to the next breathing phase.
     */
    private fun transitionToNextPhase(elapsedSeconds: Int) {
        val current = _state.value
        val pattern = current.pattern
        
        var nextPhase = current.currentPhase.next()
        var nextCycle = current.currentCycle
        
        // Check if we're starting a new cycle
        if (nextPhase == BreathPhase.INHALE) {
            nextCycle++
        }
        
        // Skip phases with 0 duration
        while (pattern.shouldSkipPhase(nextPhase)) {
            if (nextPhase == BreathPhase.HOLD_AFTER_EXHALE) {
                nextCycle++ // About to wrap to new cycle
            }
            nextPhase = nextPhase.next()
        }
        
        // Check if session is complete
        val isComplete = when (pattern.sessionType) {
            SessionType.CYCLES -> nextCycle > current.totalCycles
            SessionType.TIMED -> elapsedSeconds >= current.totalSessionSeconds
        }
        
        if (isComplete) {
            completeSession()
            return
        }
        
        // Trigger haptic feedback on phase change
        triggerHapticFeedback()
        
        // === WHERE TO ADD SOUND CUES ===
        // Call a sound playing function here:
        // playPhaseSound(nextPhase)
        // Or use TTS:
        // textToSpeech.speak(nextPhase.displayName(), ...)
        onPhaseChange(nextPhase)
        
        val nextPhaseDuration = pattern.getDurationForPhase(nextPhase)
        
        _state.value = current.copy(
            currentPhase = nextPhase,
            phaseSecondsRemaining = nextPhaseDuration,
            phaseTotalSeconds = nextPhaseDuration,
            currentCycle = nextCycle,
            elapsedSeconds = elapsedSeconds,
            phaseProgress = 0f
        )
    }
    
    /**
     * Called when the phase changes.
     * 
     * ===== ADD SOUND CUES HERE =====
     * 
     * Example implementations:
     * 
     * 1. Simple sound effect:
     *    mediaPlayer.setDataSource(context, soundUri)
     *    mediaPlayer.start()
     * 
     * 2. Text-to-speech:
     *    textToSpeech.speak(phase.displayName(), TextToSpeech.QUEUE_FLUSH, null, null)
     * 
     * 3. Different sounds per phase:
     *    when (phase) {
     *        BreathPhase.INHALE -> playSound(R.raw.inhale_chime)
     *        BreathPhase.EXHALE -> playSound(R.raw.exhale_chime)
     *        else -> playSound(R.raw.hold_tone)
     *    }
     */
    private fun onPhaseChange(phase: BreathPhase) {
        // Currently just a placeholder for sound cues
        // Add your audio implementation here
    }
    
    /**
     * Trigger haptic feedback on phase change.
     * 
     * ===== TO DISABLE HAPTICS =====
     * Simply comment out the body of this function or add a user preference check.
     */
    private fun triggerHapticFeedback() {
        vibrator?.let { vib ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Light haptic click
                vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(50)
            }
        }
    }
    
    /**
     * Mark the session as complete and record stats.
     */
    private fun completeSession() {
        timerJob?.cancel()
        recordSessionStats()
        
        // Final haptic
        triggerHapticFeedback()
        
        _state.value = _state.value.copy(
            isPlaying = false,
            isComplete = true
        )
    }
    
    /**
     * Record session statistics to DataStore.
     */
    private fun recordSessionStats() {
        if (sessionStartTime > 0) {
            val durationMinutes = ((System.currentTimeMillis() - sessionStartTime) / 60000).toInt()
                .coerceAtLeast(1) // At least 1 minute if they practiced at all
            
            viewModelScope.launch {
                statsRepository.recordSession(durationMinutes)
            }
            
            sessionStartTime = 0
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
