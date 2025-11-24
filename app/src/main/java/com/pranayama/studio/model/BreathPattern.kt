package com.pranayama.studio.model

/**
 * Represents a phase in the breathing cycle.
 * Each breath cycle consists of 4 phases: Inhale → Hold → Exhale → Hold
 */
enum class BreathPhase {
    INHALE,
    HOLD_AFTER_INHALE,
    EXHALE,
    HOLD_AFTER_EXHALE;
    
    /**
     * Returns user-friendly display name for the phase
     */
    fun displayName(): String = when (this) {
        INHALE -> "Inhale"
        HOLD_AFTER_INHALE -> "Hold"
        EXHALE -> "Exhale"
        HOLD_AFTER_EXHALE -> "Hold"
    }
    
    /**
     * Get the next phase in the breathing cycle
     */
    fun next(): BreathPhase = when (this) {
        INHALE -> HOLD_AFTER_INHALE
        HOLD_AFTER_INHALE -> EXHALE
        EXHALE -> HOLD_AFTER_EXHALE
        HOLD_AFTER_EXHALE -> INHALE
    }
}

/**
 * Session type: either complete a fixed number of cycles or breathe for a set duration
 */
enum class SessionType {
    CYCLES,  // Complete X breathing cycles
    TIMED    // Breathe for X minutes
}

/**
 * Represents a pranayama breathing pattern.
 * 
 * @param id Unique identifier for the pattern
 * @param name Display name (e.g., "Box Breathing")
 * @param description Short description of the technique's benefits
 * @param inhaleSeconds Duration of inhale phase in seconds
 * @param holdAfterInhaleSeconds Duration of hold after inhale (0 to skip)
 * @param exhaleSeconds Duration of exhale phase in seconds
 * @param holdAfterExhaleSeconds Duration of hold after exhale (0 to skip)
 * @param sessionType Whether session is cycle-based or time-based
 * @param defaultCycles Default number of cycles (used when sessionType is CYCLES)
 * @param defaultMinutes Default duration in minutes (used when sessionType is TIMED)
 * @param isCustom Whether this is a user-customizable pattern
 * 
 * ===== HOW TO ADD NEW PATTERNS =====
 * Simply create a new BreathPattern instance and add it to the PranayamaPatterns.allPatterns list below.
 * Example:
 *   BreathPattern(
 *       id = "my_pattern",
 *       name = "My Pattern",
 *       description = "Description here",
 *       inhaleSeconds = 4,
 *       holdAfterInhaleSeconds = 4,
 *       exhaleSeconds = 4,
 *       holdAfterExhaleSeconds = 0,  // No hold after exhale
 *       defaultCycles = 10
 *   )
 */
data class BreathPattern(
    val id: String,
    val name: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdAfterInhaleSeconds: Int,
    val exhaleSeconds: Int,
    val holdAfterExhaleSeconds: Int,
    val sessionType: SessionType = SessionType.TIMED,
    val defaultCycles: Int = 10,
    val defaultMinutes: Int = 5,
    val isCustom: Boolean = false
) {
    /**
     * Total duration of one complete breath cycle in seconds
     */
    val cycleDurationSeconds: Int
        get() = inhaleSeconds + holdAfterInhaleSeconds + exhaleSeconds + holdAfterExhaleSeconds
    
    /**
     * Returns formatted timing string like "4-4-4-4" or "4-7-8"
     */
    fun timingString(): String {
        val parts = mutableListOf<Int>()
        parts.add(inhaleSeconds)
        if (holdAfterInhaleSeconds > 0) parts.add(holdAfterInhaleSeconds)
        parts.add(exhaleSeconds)
        if (holdAfterExhaleSeconds > 0) parts.add(holdAfterExhaleSeconds)
        return parts.joinToString("-")
    }
    
    /**
     * Returns a summary string like "4-4-4-4, 5 min"
     */
    fun summaryString(): String {
        val timing = timingString()
        val duration = if (sessionType == SessionType.TIMED) {
            "$defaultMinutes min"
        } else {
            "$defaultCycles cycles"
        }
        return "$timing, $duration"
    }
    
    /**
     * Get the duration in seconds for a specific phase
     */
    fun getDurationForPhase(phase: BreathPhase): Int = when (phase) {
        BreathPhase.INHALE -> inhaleSeconds
        BreathPhase.HOLD_AFTER_INHALE -> holdAfterInhaleSeconds
        BreathPhase.EXHALE -> exhaleSeconds
        BreathPhase.HOLD_AFTER_EXHALE -> holdAfterExhaleSeconds
    }
    
    /**
     * Check if a phase should be skipped (duration is 0)
     */
    fun shouldSkipPhase(phase: BreathPhase): Boolean = getDurationForPhase(phase) == 0
}

/**
 * Predefined pranayama patterns.
 * 
 * ===== TO ADD MORE PATTERNS =====
 * Simply add a new BreathPattern to the allPatterns list below.
 */
object PranayamaPatterns {
    
    val boxBreathing = BreathPattern(
        id = "box_breathing",
        name = "Box Breathing",
        description = "Calming focus • Used by Navy SEALs",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 4,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 4,
        sessionType = SessionType.TIMED,
        defaultMinutes = 5
    )
    
    val breathing478 = BreathPattern(
        id = "4_7_8",
        name = "4-7-8 Breathing",
        description = "Sleep support • Deep relaxation",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 7,
        exhaleSeconds = 8,
        holdAfterExhaleSeconds = 0,  // No hold after exhale in this pattern
        sessionType = SessionType.CYCLES,
        defaultCycles = 4  // Traditionally done in sets of 4
    )
    
    val nadiShodhana = BreathPattern(
        id = "nadi_shodhana",
        name = "Alternate Nostril",
        description = "Balance & clarity • Simplified version",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 4,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 4,
        sessionType = SessionType.TIMED,
        defaultMinutes = 5
    )
    
    val customPattern = BreathPattern(
        id = "custom",
        name = "Custom",
        description = "Create your own pattern",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 4,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 4,
        sessionType = SessionType.TIMED,
        defaultMinutes = 5,
        isCustom = true
    )
    
    /**
     * List of all available patterns.
     * Add new patterns here to make them available in the app.
     */
    val allPatterns: List<BreathPattern> = listOf(
        boxBreathing,
        breathing478,
        nadiShodhana,
        customPattern
    )
    
    /**
     * Find a pattern by its ID
     */
    fun findById(id: String): BreathPattern? = allPatterns.find { it.id == id }
}
