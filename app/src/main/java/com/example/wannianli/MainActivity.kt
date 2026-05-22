package com.example.wannianli

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.wannianli.ui.navigation.AppNavigation
import com.example.wannianli.ui.navigation.NavRoutes
import com.example.wannianli.ui.screen.DISCLAIMER_TEXT
import com.example.wannianli.ui.theme.WannianliTheme
import com.example.wannianli.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
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
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences("disclaimer_prefs", Context.MODE_PRIVATE) }
    var disclaimerAgreed by remember { mutableStateOf(prefs.getBoolean("agreed", false)) }
    var disclaimerChecked by remember { mutableStateOf(false) }

    if (!disclaimerAgreed) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        prefs.edit().putBoolean("agreed", true).apply()
                        disclaimerAgreed = true
                    },
                    enabled = disclaimerChecked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("确认并同意", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = null,
            title = {
                Text(
                    text = "免责声明",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = DISCLAIMER_TEXT,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { disclaimerChecked = !disclaimerChecked },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = disclaimerChecked,
                            onCheckedChange = { disclaimerChecked = it }
                        )
                        Text(
                            text = "我已阅读并同意以上免责声明（首次启动需勾选确认）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "万年历",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "v1.0.38",
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
                    label = { Text("首页") },
                    selected = navController.currentDestination?.route == NavRoutes.HOME,
                    onClick = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.Book, contentDescription = null)
                    },
                    label = { Text("日课应验笔记") },
                    selected = navController.currentDestination?.route == NavRoutes.NOTE_LIST,
                    onClick = {
                        navController.navigate(NavRoutes.NOTE_LIST) {
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.History, contentDescription = null)
                    },
                    label = { Text("更新迭代") },
                    selected = navController.currentDestination?.route == NavRoutes.CHANGELOG,
                    onClick = {
                        navController.navigate(NavRoutes.CHANGELOG) {
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.Gavel, contentDescription = null)
                    },
                    label = { Text("免责声明") },
                    selected = navController.currentDestination?.route == NavRoutes.DISCLAIMER,
                    onClick = {
                        navController.navigate(NavRoutes.DISCLAIMER) {
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    label = { Text("设置") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
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