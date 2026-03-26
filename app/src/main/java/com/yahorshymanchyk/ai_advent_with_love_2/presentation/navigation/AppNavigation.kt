package com.yahorshymanchyk.ai_advent_with_love_2.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.chats.ChatsScreen
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.home.HomeScreen
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.home.HomeViewModel
import com.yahorshymanchyk.ai_advent_with_love_2.presentation.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Home, Screen.Chats, Screen.Settings)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Scoped to the Activity — shared between HomeScreen and ChatsScreen
    val homeViewModel: HomeViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            composable(Screen.Home.route) {
                HomeScreen(paddingValues = innerPadding, viewModel = homeViewModel)
            }
            composable(Screen.Chats.route) {
                ChatsScreen(
                    paddingValues = innerPadding,
                    onChatSelected = { chatId ->
                        homeViewModel.loadChat(chatId)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(paddingValues = innerPadding)
            }
        }
    }
}
