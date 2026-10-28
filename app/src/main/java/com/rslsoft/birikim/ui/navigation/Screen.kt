package com.rslsoft.birikim.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Calculator : Screen("calculator")
    data object History : Screen("history")
    data object Goals : Screen("goals")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val iconKey: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Ana Sayfa", "home"),
    BottomNavItem(Screen.Calculator, "Hesap", "calculate"),
    BottomNavItem(Screen.History, "Geçmiş", "history"),
    BottomNavItem(Screen.Goals, "Hedefler", "flag")
)
