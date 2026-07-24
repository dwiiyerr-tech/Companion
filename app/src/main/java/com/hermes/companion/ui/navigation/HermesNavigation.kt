package com.hermes.companion.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hermes.companion.ui.screen.AgentScreen
import com.hermes.companion.ui.screen.AndroidControlScreen
import com.hermes.companion.ui.screen.BrowserScreen
import com.hermes.companion.ui.screen.DeveloperScreen
import com.hermes.companion.ui.screen.HomeScreen
import com.hermes.companion.ui.screen.LogsScreen
import com.hermes.companion.ui.screen.MemoryScreen
import com.hermes.companion.ui.screen.MissionScreen
import com.hermes.companion.ui.screen.PerformanceScreen
import com.hermes.companion.ui.screen.PluginsScreen
import com.hermes.companion.ui.screen.SettingsScreen
import com.hermes.companion.ui.screen.ToolsScreen

@Composable
fun HermesCompanionApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                HermesBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate(Routes.DEVELOPER) }
                )
            }
            composable(Screen.Mission.route) {
                MissionScreen()
            }
            composable(Screen.Agents.route) {
                AgentScreen(
                    onNavigateToAndroidControl = { navController.navigate(Routes.ANDROID_CONTROL) },
                    onNavigateToBrowser = { navController.navigate(Routes.BROWSER) }
                )
            }
            composable(Screen.Memory.route) {
                MemoryScreen()
            }
            composable(Screen.Tools.route) {
                ToolsScreen(
                    onNavigateToPlugins = { navController.navigate(Routes.PLUGINS) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToLogs = { navController.navigate(Routes.LOGS) },
                    onNavigateToPerformance = { navController.navigate(Routes.PERFORMANCE) },
                    onNavigateToDeveloper = { navController.navigate(Routes.DEVELOPER) }
                )
            }
            composable(Routes.ANDROID_CONTROL) {
                AndroidControlScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BROWSER) {
                BrowserScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PLUGINS) {
                PluginsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.LOGS) {
                LogsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PERFORMANCE) {
                PerformanceScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.DEVELOPER) {
                DeveloperScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
