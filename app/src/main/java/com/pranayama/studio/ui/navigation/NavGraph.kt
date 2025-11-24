package com.pranayama.studio.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pranayama.studio.ui.home.HomeScreen
import com.pranayama.studio.ui.session.SessionScreen
import com.pranayama.studio.viewmodel.SessionViewModel

/**
 * Navigation routes for the app.
 */
object Routes {
    const val HOME = "home"
    const val SESSION = "session/{patternId}"
    
    fun sessionRoute(patternId: String) = "session/$patternId"
}

/**
 * Main navigation graph for Pranayama Studio.
 * 
 * Navigation flow:
 * Home → Session (with pattern ID)
 * Session → Home (on complete/back)
 */
@Composable
fun PranayamaNavGraph(
    navController: NavHostController,
    sessionViewModel: SessionViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        // Home Screen - shows list of breathing patterns
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = sessionViewModel,
                onPatternSelected = { patternId ->
                    navController.navigate(Routes.sessionRoute(patternId))
                },
                onSettingsClick = {
                    // TODO: Navigate to settings screen when implemented
                    // For now, this is a placeholder
                }
            )
        }
        
        // Session Screen - breathing exercise with pacer
        composable(
            route = Routes.SESSION,
            arguments = listOf(
                navArgument("patternId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patternId = backStackEntry.arguments?.getString("patternId") ?: "box_breathing"
            
            SessionScreen(
                patternId = patternId,
                viewModel = sessionViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
