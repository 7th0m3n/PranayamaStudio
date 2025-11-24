package com.pranayama.studio.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Pranayama Studio Theme
 * 
 * A calm, focused dark theme optimized for breathing exercises.
 * The dark background reduces eye strain during meditation/breathing sessions.
 * 
 * To switch to light theme or add dynamic colors, modify the PranayamaStudioTheme function.
 */

private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimaryTeal,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryTealDark,
    onPrimaryContainer = TextPrimary,
    
    // Secondary colors  
    secondary = SecondaryAmber,
    onSecondary = DarkBackground,
    secondaryContainer = SecondaryAmberDark,
    onSecondaryContainer = TextPrimary,
    
    // Tertiary (using exhale green)
    tertiary = ExhaleColor,
    onTertiary = DarkBackground,
    
    // Background colors
    background = DarkBackground,
    onBackground = TextPrimary,
    
    // Surface colors
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    // Other
    outline = TextMuted,
    outlineVariant = DarkSurfaceVariant,
    
    // Error
    error = ErrorRed,
    onError = Color.White
)

// Light color scheme (for future use)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryTealDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryTealLight,
    onPrimaryContainer = DarkBackground,
    
    secondary = SecondaryAmberDark,
    onSecondary = Color.White,
    secondaryContainer = SecondaryAmberLight,
    onSecondaryContainer = DarkBackground,
    
    background = LightBackground,
    onBackground = DarkBackground,
    surface = LightSurface,
    onSurface = DarkBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextMuted
)

/**
 * Main theme composable for the app.
 * 
 * @param darkTheme Force dark theme. Default is true for this app since
 *                  dark mode is optimal for breathing/meditation exercises.
 * @param content The composable content to be themed.
 */
@Composable
fun PranayamaStudioTheme(
    darkTheme: Boolean = true, // Default to dark for calm breathing experience
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar transparent and match theme
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            // Set light/dark status bar icons
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
