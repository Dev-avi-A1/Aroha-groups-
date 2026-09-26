package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArohaViewModel
import com.example.ui.viewmodel.DashboardViewModel

@Composable
fun ArohaAppNavigation(
    viewModel: ArohaViewModel,
    dashboardViewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = DashboardViewModel.provideFactory(viewModel.repository)
    )
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isDistractionFree by viewModel.isDistractionFree.collectAsState()
    val user by viewModel.userState.collectAsState()

    // Determine if bottom bar should be visible
    val showBottomBar = remember(currentRoute, isDistractionFree) {
        if (isDistractionFree) false
        else when (currentRoute) {
            Screen.Dashboard.route,
            Screen.Home.route,
            Screen.Journal.route,
            Screen.Coaching.route,
            Screen.AICoach.route,
            Screen.Goals.route,
            Screen.Focus.route,
            Screen.Profile.route -> true
            else -> false
        }
    }

    val startDestination = remember(user) {
        if (user != null && !user!!.isOnboarded) Screen.Onboarding.route
        else Screen.Dashboard.route
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = ArohaDarkSurface,
                    contentColor = ArohaTextPrimary,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("aroha_bottom_bar")
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        val iconVector = if (isSelected) {
                            screen.selectedIcon ?: Icons.Filled.Home
                        } else {
                            screen.unselectedIcon ?: Icons.Outlined.Home
                        }
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ArohaTeal,
                                selectedTextColor = ArohaTeal,
                                unselectedIconColor = ArohaTextMuted,
                                unselectedTextColor = ArohaTextMuted,
                                indicatorColor = ArohaTeal.copy(alpha = 0.15f)
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = ArohaDeepBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Dashboard / Home
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                    onStartFocus = { title ->
                        viewModel.setSelectedFocusGoal(title)
                        navController.navigate(Screen.Focus.route)
                    }
                )
            }
            composable(Screen.Home.route) {
                DashboardScreen(
                    navController = navController,
                    dashboardViewModel = dashboardViewModel,
                    onStartFocus = { title ->
                        viewModel.setSelectedFocusGoal(title)
                        navController.navigate(Screen.Focus.route)
                    }
                )
            }

            // Journal
            composable(Screen.Journal.route) {
                JournalScreen(navController = navController, viewModel = viewModel)
            }

            // Coaching / AI Coach
            composable(Screen.Coaching.route) {
                AICoachScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.AICoach.route) {
                AICoachScreen(navController = navController, viewModel = viewModel)
            }

            composable(Screen.Goals.route) {
                GoalsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Focus.route) {
                FocusScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController, viewModel = viewModel)
            }

            // Secondary screens
            composable(Screen.Habits.route) {
                BackHandler { navController.popBackStack() }
                HabitsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Evolution.route) {
                BackHandler { navController.popBackStack() }
                EvolutionScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Analytics.route) {
                BackHandler { navController.popBackStack() }
                AnalyticsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                BackHandler { navController.popBackStack() }
                SettingsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(navController = navController, viewModel = viewModel)
            }
        }
    }
}
