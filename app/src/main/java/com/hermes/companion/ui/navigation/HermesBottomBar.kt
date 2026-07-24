package com.hermes.companion.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agent
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Agent
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.hermes.companion.ui.theme.HermesPurple
import com.hermes.companion.ui.theme.HermesPurpleDark
import com.hermes.companion.ui.theme.HermesPurpleLight
import com.hermes.companion.ui.theme.DarkOnSurfaceVariant

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Mission : Screen("mission", "Mission", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow)
    data object Agents : Screen("agents", "Agents", Icons.Filled.Agent, Icons.Outlined.Agent)
    data object Memory : Screen("memory", "Memory", Icons.Filled.Memory, Icons.Outlined.Memory)
    data object Tools : Screen("tools", "Tools", Icons.Filled.Build, Icons.Outlined.Build)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

// Secondary screens (not in bottom bar)
object Routes {
    const val ANDROID_CONTROL = "android_control"
    const val BROWSER = "browser"
    const val PLUGINS = "plugins"
    const val LOGS = "logs"
    const val PERFORMANCE = "performance"
    const val DEVELOPER = "developer"
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Mission,
    Screen.Agents,
    Screen.Memory,
    Screen.Tools,
    Screen.Settings
)

@Composable
fun HermesBottomBar(
    navController: NavHostController
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    NavigationBar(
        containerColor = HermesPurpleDark.copy(alpha = 0.95f)
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HermesPurpleLight,
                    selectedTextColor = HermesPurpleLight,
                    unselectedIconColor = DarkOnSurfaceVariant,
                    unselectedTextColor = DarkOnSurfaceVariant,
                    indicatorColor = HermesPurple.copy(alpha = 0.3f)
                ),
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
