package com.yahorshymanchyk.ai_advent_with_love_2.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Chats : Screen("chats", "Chats", Icons.Default.Email)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
