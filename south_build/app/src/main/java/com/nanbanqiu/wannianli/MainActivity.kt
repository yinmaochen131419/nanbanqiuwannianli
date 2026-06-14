/*
 * Copyright (c) 2025-2026 南半球历法 (Nanbanqiu Wannianli)
 * All rights reserved.
 */
package com.nanbanqiu.wannianli

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.nanbanqiu.wannianli.R
import com.nanbanqiu.wannianli.ui.navigation.AppNavigation
import com.nanbanqiu.wannianli.ui.navigation.NavRoutes
import com.nanbanqiu.wannianli.ui.theme.WannianliTheme
import com.nanbanqiu.wannianli.util.LanguageHelper
import com.nanbanqiu.wannianli.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val langCode = LanguageHelper.getSavedLanguage(newBase)
        val context = LanguageHelper.applyLanguage(newBase, langCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WannianliTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val viewModel: MainViewModel = viewModel()
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = stringResource(R.string.drawer_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "v1.0.55",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Home, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_home)) },
                        selected = navController.currentDestination?.route == NavRoutes.HOME,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(NavRoutes.HOME) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_schedule)) },
                        selected = navController.currentDestination?.route == NavRoutes.SCHEDULE,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.SCHEDULE) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_changelog)) },
                        selected = navController.currentDestination?.route == NavRoutes.CHANGELOG,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.CHANGELOG) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.School, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_knowledge)) },
                        selected = navController.currentDestination?.route == NavRoutes.KNOWLEDGE,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.KNOWLEDGE) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_settings)) },
                        selected = navController.currentDestination?.route == NavRoutes.SETTINGS,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.SETTINGS) { launchSingleTop = true }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Feedback, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_feedback)) },
                        selected = navController.currentDestination?.route == NavRoutes.FEEDBACK,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.FEEDBACK) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Language, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.lang_settings)) },
                        selected = navController.currentDestination?.route == NavRoutes.LANGUAGE,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.LANGUAGE) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Info, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.menu_disclaimer)) },
                        selected = navController.currentDestination?.route == NavRoutes.DISCLAIMER,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.DISCLAIMER) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Shield, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.title_privacy_policy)) },
                        selected = navController.currentDestination?.route == NavRoutes.PRIVACY_POLICY,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.PRIVACY_POLICY) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = {
                            Icon(Icons.Default.Code, contentDescription = null)
                        },
                        label = { Text(stringResource(R.string.title_opensource)) },
                        selected = navController.currentDestination?.route == NavRoutes.OPENSOURCE,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                navController.navigate(NavRoutes.OPENSOURCE) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    ) {
        AppNavigation(
            navController = navController,
            viewModel = viewModel,
            onDrawerToggle = {
                scope.launch { drawerState.open() }
            }
        )
    }
}