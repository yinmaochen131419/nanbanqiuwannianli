package com.example.wannianli.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wannianli.ui.screen.ChangelogScreen
import com.example.wannianli.ui.screen.DisclaimerScreen
import com.example.wannianli.ui.screen.HomeScreen
import com.example.wannianli.ui.screen.NoteEditScreen
import com.example.wannianli.ui.screen.NoteListScreen
import com.example.wannianli.viewmodel.MainViewModel
import com.example.wannianli.viewmodel.NoteViewModel

object NavRoutes {
    const val HOME = "home"
    const val CHANGELOG = "changelog"
    const val NOTE_LIST = "note_list"
    const val NOTE_EDIT = "note_edit/{noteId}"
    const val NOTE_NEW = "note_new"
    const val DISCLAIMER = "disclaimer"

    fun noteEdit(id: Long) = "note_edit/$id"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel,
    onDrawerToggle: () -> Unit
) {
    val noteViewModel: NoteViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onDrawerToggle = onDrawerToggle
            )
        }
        composable(NavRoutes.CHANGELOG) {
            ChangelogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.DISCLAIMER) {
            DisclaimerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.NOTE_LIST) {
            NoteListScreen(
                viewModel = noteViewModel,
                onBack = { navController.popBackStack() },
                onEdit = { noteId ->
                    if (noteId != null) {
                        navController.navigate(NavRoutes.noteEdit(noteId))
                    } else {
                        navController.navigate(NavRoutes.NOTE_NEW)
                    }
                }
            )
        }
        composable(NavRoutes.NOTE_NEW) {
            NoteEditScreen(
                noteId = null,
                noteViewModel = noteViewModel,
                mainViewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = NavRoutes.NOTE_EDIT,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId")
            NoteEditScreen(
                noteId = noteId,
                noteViewModel = noteViewModel,
                mainViewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}