package com.pranayama.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pranayama.studio.ui.navigation.PranayamaNavGraph
import com.pranayama.studio.ui.theme.PranayamaStudioTheme

/**
 * Main Activity for Pranayama Studio.
 * 
 * Uses single-activity architecture with Jetpack Compose Navigation.
 * The entire UI is rendered in Compose with a dark theme optimized
 * for breathing exercises.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        setContent {
            PranayamaStudioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    PranayamaNavGraph(navController = navController)
                }
            }
        }
    }
}
