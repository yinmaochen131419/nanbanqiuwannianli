/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nanbanqiu.wannianli.ui.screen.ChangelogScreen
import com.nanbanqiu.wannianli.ui.screen.EclipseScreen
import com.nanbanqiu.wannianli.ui.screen.HomeScreen
import com.nanbanqiu.wannianli.ui.screen.DisclaimerScreen
import com.nanbanqiu.wannianli.ui.screen.FeedbackScreen
import com.nanbanqiu.wannianli.ui.screen.KnowledgeScreen
import com.nanbanqiu.wannianli.ui.screen.NoteEditScreen
import com.nanbanqiu.wannianli.ui.screen.NoteListScreen
import com.nanbanqiu.wannianli.ui.screen.OpenSourceScreen
import com.nanbanqiu.wannianli.ui.screen.PrivacyPolicyScreen

import com.nanbanqiu.wannianli.ui.screen.LanguageSettingsScreen
import com.nanbanqiu.wannianli.ui.screen.ScheduleScreen
import com.nanbanqiu.wannianli.ui.screen.SettingsScreen
import com.nanbanqiu.wannianli.ui.screen.ZeRiScreen
import com.nanbanqiu.wannianli.viewmodel.MainViewModel
import com.nanbanqiu.wannianli.viewmodel.NoteViewModel

object NavRoutes {
    const val HOME = "home"
    const val ZE_RI = "ze_ri"
    const val ECLIPSE = "eclipse"
    const val SCHEDULE = "schedule"
    const val NOTE_LIST = "note_list"
    const val NOTE_EDIT = "note_edit/{noteId}"
    const val NOTE_NEW = "note_new"
    const val CHANGELOG = "changelog"
    const val KNOWLEDGE = "knowledge"
    const val DISCLAIMER = "disclaimer"
    const val PRIVACY_POLICY = "privacy_policy"
    const val OPENSOURCE = "opensource"
    const val SETTINGS = "settings"
    const val LANGUAGE = "language"
    const val FEEDBACK = "feedback"

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
                onDrawerToggle = onDrawerToggle,
                onNavigateToSchedule = { navController.navigate(NavRoutes.SCHEDULE) { launchSingleTop = true } }
            )
        }
        composable(NavRoutes.ZE_RI) {
            ZeRiScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.ECLIPSE) {
            EclipseScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.SCHEDULE) {
            ScheduleScreen(
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
        composable(NavRoutes.CHANGELOG) {
            ChangelogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.KNOWLEDGE) {
            KnowledgeScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.DISCLAIMER) {
            DisclaimerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.OPENSOURCE) {
            OpenSourceScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                northCityId = viewModel.northCityId.value,
                onNorthCityChanged = { viewModel.setNorthCity(it) }
            )
        }
        composable(NavRoutes.LANGUAGE) {
            LanguageSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.FEEDBACK) {
            FeedbackScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}