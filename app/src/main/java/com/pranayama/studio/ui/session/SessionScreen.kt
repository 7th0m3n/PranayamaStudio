package com.pranayama.studio.ui.session

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pranayama.studio.ui.components.BreathPacer
import com.pranayama.studio.ui.components.SessionProgress
import com.pranayama.studio.viewmodel.SessionViewModel

/**
 * Session Screen - The breathing exercise interface.
 * 
 * Layout (top to bottom):
 * - Back button and pattern name
 * - Breath pacer animation (center, largest element)
 * - Progress indicator
 * - Play/Pause control
 * - End session button
 * 
 * The screen is designed for one-thumb operation with controls at the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    patternId: String,
    viewModel: SessionViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Initialize session when screen loads
    LaunchedEffect(patternId) {
        viewModel.initSession(patternId)
    }
    
    // Handle completion
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            // Could show a completion dialog or auto-navigate
            // For now, user must tap back or reset
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Subtle gradient background for depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background
                            ),
                            radius = 800f
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with back button and pattern info
                SessionTopBar(
                    patternName = state.pattern.name,
                    patternDescription = state.pattern.description,
                    onBackClick = {
                        viewModel.pause()
                        onNavigateBack()
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Main content area with pacer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isComplete) {
                        // Session complete view
                        SessionCompleteView(
                            onRestart = { viewModel.resetSession() },
                            onFinish = onNavigateBack
                        )
                    } else {
                        // Breath pacer
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            BreathPacer(
                                state = state,
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .weight(1f, fill = false)
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Progress indicator
                            SessionProgress(
                                state = state,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Controls at bottom for thumb reach
                if (!state.isComplete) {
                    SessionControls(
                        isPlaying = state.isPlaying,
                        onPlayPause = { viewModel.togglePlayPause() },
                        onEnd = { viewModel.endSession() }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * Top bar showing pattern info and back button.
 */
@Composable
fun SessionTopBar(
    patternName: String,
    patternDescription: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Pattern info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = patternName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = patternDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Play/Pause and End Session controls.
 */
@Composable
fun SessionControls(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onEnd: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Play/Pause button
        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(72.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // End session text button
        TextButton(onClick = onEnd) {
            Text(
                text = "End Session",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * View shown when session is complete.
 */
@Composable
fun SessionCompleteView(
    onRestart: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration emoji/icon
        Text(
            text = "🧘",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Session Complete",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Great work! Take a moment to\nnotice how you feel.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Restart button
            OutlinedButton(
                onClick = onRestart,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Again")
            }
            
            // Done button
            Button(
                onClick = onFinish,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Done")
            }
        }
    }
}
