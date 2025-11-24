package com.pranayama.studio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pranayama.studio.model.BreathPhase
import com.pranayama.studio.ui.theme.ExhaleColor
import com.pranayama.studio.ui.theme.HoldColor
import com.pranayama.studio.ui.theme.InhaleColor
import com.pranayama.studio.viewmodel.SessionState

/**
 * Animated breath pacer circle that expands/contracts with breathing phases.
 * 
 * ===== HOW THE PACER ANIMATION WORKS =====
 * 
 * The pacer shows breathing state through:
 * 1. Circle SIZE: Expands on inhale, contracts on exhale, stays static on hold
 * 2. COLOR: Changes based on current phase (inhale=blue, hold=amber, exhale=green)
 * 3. GLOW: Subtle pulsing glow effect for visual interest
 * 
 * ===== TO CUSTOMIZE THE PACER =====
 * 
 * - Change colors: Modify getPhaseColor() function
 * - Change size range: Adjust minRadius/maxRadius calculations
 * - Change animation: Modify the animatedRadius calculation
 * - Add ring effects: Draw additional circles in Canvas
 * - Add particles: Create separate composable with animations
 * 
 * @param state Current session state from ViewModel
 * @param modifier Modifier for sizing/positioning
 */
@Composable
fun BreathPacer(
    state: SessionState,
    modifier: Modifier = Modifier
) {
    val phaseColor = getPhaseColor(state.currentPhase)
    
    // Smooth animated transition for breath fullness
    val animatedBreathProgress by animateFloatAsState(
        targetValue = state.breathProgress,
        animationSpec = tween(
            durationMillis = 100,
            easing = LinearEasing
        ),
        label = "breathProgress"
    )
    
    // Subtle glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Background glow canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = size.minDimension / 2 * 0.85f
            val minRadius = size.minDimension / 2 * 0.35f
            
            // Calculate current radius based on breath progress
            val currentRadius = minRadius + (maxRadius - minRadius) * animatedBreathProgress
            
            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = glowAlpha * 0.3f),
                        phaseColor.copy(alpha = 0f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = currentRadius * 1.5f
                ),
                radius = currentRadius * 1.5f,
                center = Offset(centerX, centerY)
            )
            
            // Main circle fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        phaseColor.copy(alpha = 0.2f),
                        phaseColor.copy(alpha = 0.05f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = currentRadius
                ),
                radius = currentRadius,
                center = Offset(centerX, centerY)
            )
            
            // Circle outline
            drawCircle(
                color = phaseColor.copy(alpha = 0.8f),
                radius = currentRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            
            // Inner glow ring
            drawCircle(
                color = phaseColor.copy(alpha = glowAlpha),
                radius = currentRadius - 8.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Center text content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Phase name
            Text(
                text = state.currentPhase.displayName(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = phaseColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Countdown
            Text(
                text = "${state.phaseSecondsRemaining}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/**
 * Get the color for a breathing phase.
 * 
 * ===== TO CHANGE PHASE COLORS =====
 * Modify the colors returned here. You can also use
 * different color schemes based on pattern type.
 */
@Composable
fun getPhaseColor(phase: BreathPhase): Color {
    return when (phase) {
        BreathPhase.INHALE -> InhaleColor  // Bright blue - expanding, energizing
        BreathPhase.HOLD_AFTER_INHALE -> HoldColor  // Warm amber - stillness
        BreathPhase.EXHALE -> ExhaleColor  // Soft green - releasing, calming
        BreathPhase.HOLD_AFTER_EXHALE -> HoldColor  // Warm amber - stillness
    }
}

/**
 * Simple progress indicator showing cycle or time progress.
 */
@Composable
fun SessionProgress(
    state: SessionState,
    modifier: Modifier = Modifier
) {
    val progressText = if (state.pattern.sessionType == com.pranayama.studio.model.SessionType.CYCLES) {
        "Cycle ${state.currentCycle} of ${state.totalCycles}"
    } else {
        val remaining = state.totalSessionSeconds - state.elapsedSeconds
        val minutes = remaining / 60
        val seconds = remaining % 60
        "${minutes}:${seconds.toString().padStart(2, '0')} remaining"
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = progressText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Progress bar
        val progress = state.elapsedSeconds.toFloat() / state.totalSessionSeconds.toFloat()
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
