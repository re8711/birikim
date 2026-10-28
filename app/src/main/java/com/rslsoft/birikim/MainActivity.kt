package com.rslsoft.birikim

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rslsoft.birikim.data.local.PreferencesDataStore
import com.rslsoft.birikim.ui.components.BiometricHelper
import com.rslsoft.birikim.ui.components.BiometricLockScreen
import com.rslsoft.birikim.ui.components.HBottomBar
import com.rslsoft.birikim.ui.navigation.Screen
import com.rslsoft.birikim.ui.screens.calculator.CalculatorScreen
import com.rslsoft.birikim.ui.screens.calculator.CalculatorViewModel
import com.rslsoft.birikim.ui.screens.goals.GoalsScreen
import com.rslsoft.birikim.ui.screens.goals.GoalsViewModel
import com.rslsoft.birikim.ui.screens.history.HistoryScreen
import com.rslsoft.birikim.ui.screens.history.HistoryViewModel
import com.rslsoft.birikim.ui.screens.home.HomeScreen
import com.rslsoft.birikim.ui.screens.home.HomeViewModel
import com.rslsoft.birikim.ui.screens.settings.SettingsScreen
import com.rslsoft.birikim.ui.screens.settings.SettingsViewModel
import com.rslsoft.birikim.ui.theme.BirikimTheme
import com.rslsoft.birikim.worker.ReminderWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    private var lastBackgroundTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Notification channel and periodic check
        ReminderWorker.createNotificationChannel(this)
        ReminderWorker.schedulePeriodicCheck(this)

        setContent {
            val themeMode by preferencesDataStore.themeMode.collectAsState(initial = "SYSTEM")
            val biometricEnabled by preferencesDataStore.biometricEnabled.collectAsState(initial = false)

            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            var isLocked by remember { mutableStateOf(biometricEnabled) }

            // Lock on launch if enabled
            LaunchedEffect(biometricEnabled) {
                if (biometricEnabled && BiometricHelper.canAuthenticate(this@MainActivity)) {
                    isLocked = true
                    BiometricHelper.setSecureFlag(this@MainActivity, true)
                } else {
                    isLocked = false
                    BiometricHelper.setSecureFlag(this@MainActivity, false)
                }
            }

            BirikimTheme(darkTheme = darkTheme) {
                if (isLocked) {
                    BiometricLockScreen(
                        onAuthenticate = {
                            BiometricHelper.authenticate(
                                activity = this@MainActivity,
                                onSuccess = {
                                    isLocked = false
                                    BiometricHelper.setSecureFlag(this@MainActivity, false)
                                },
                                onError = { /* handle error */ }
                            )
                        }
                    )
                } else {
                    MainAppContent()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lastBackgroundTime = SystemClock.elapsedRealtime()
    }

    override fun onResume() {
        super.onResume()
        // Re-lock if backgrounded for more than 30 seconds
        if (lastBackgroundTime > 0) {
            val elapsed = SystemClock.elapsedRealtime() - lastBackgroundTime
            if (elapsed > 30_000) {
                // Should re-check biometric setting and lock
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val currentScreen = when (currentRoute) {
        Screen.Home.route -> Screen.Home
        Screen.Calculator.route -> Screen.Calculator
        Screen.History.route -> Screen.History
        Screen.Goals.route -> Screen.Goals
        Screen.Settings.route -> Screen.Settings
        else -> Screen.Home
    }

    val showBottomBar = currentScreen != Screen.Settings

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HBottomBar(
                    selectedScreen = currentScreen,
                    onScreenSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = BirikimTheme.colors.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) }
                )
            }

            composable(Screen.Calculator.route) {
                val calculatorViewModel: CalculatorViewModel = hiltViewModel()
                CalculatorScreen(viewModel = calculatorViewModel)
            }

            composable(Screen.History.route) {
                val historyViewModel: HistoryViewModel = hiltViewModel()
                HistoryScreen(viewModel = historyViewModel)
            }

            composable(Screen.Goals.route) {
                val goalsViewModel: GoalsViewModel = hiltViewModel()
                GoalsScreen(viewModel = goalsViewModel)
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
