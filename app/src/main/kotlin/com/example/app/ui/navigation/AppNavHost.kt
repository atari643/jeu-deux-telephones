package com.example.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.ui.about.AboutRoute
import com.example.app.ui.notes.NotesRoute
import kotlinx.serialization.Serializable

/** Une destination = un objet (ou data class si arguments) @Serializable. */
@Serializable
data object NotesDestination

@Serializable
data object AboutDestination

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = NotesDestination) {
        composable<NotesDestination> {
            NotesRoute(onNavigateToAbout = { navController.navigate(AboutDestination) })
        }
        composable<AboutDestination> {
            AboutRoute(onBackClick = { navController.popBackStack() })
        }
    }
}
