package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Home : Screen("home", "Dashboard", Icons.Filled.Home, Icons.Outlined.Home)
    object Journal : Screen("journal", "Journal", Icons.Filled.Book, Icons.Outlined.Book)
    object Coaching : Screen("coaching", "Coaching", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object AICoach : Screen("ai_coach", "Coaching", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Goals : Screen("goals", "Goals", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Focus : Screen("focus", "Focus", Icons.Filled.Timer, Icons.Outlined.Timer)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)

    // Secondary routes
    object Habits : Screen("habits", "Habits")
    object Evolution : Screen("evolution", "Evolution")
    object Analytics : Screen("analytics", "Analytics")
    object Settings : Screen("settings", "Settings")
    object Onboarding : Screen("onboarding", "Onboarding")

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Dashboard, Journal, Coaching, Focus, Profile)
    }
}
