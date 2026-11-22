package com.nirapod.family.parent.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nirapod.family.parent.ui.auth.LoginScreen
import com.nirapod.family.parent.ui.auth.RegisterScreen
import com.nirapod.family.parent.ui.dashboard.DashboardScreen
import com.nirapod.family.parent.ui.devices.DevicesScreen
import com.nirapod.family.parent.ui.license.LicenseScreen
import com.nirapod.family.parent.ui.location.LocationMapScreen
import com.nirapod.family.parent.ui.monitor.AudioMonitorScreen
import com.nirapod.family.parent.ui.monitor.CameraViewScreen
import com.nirapod.family.parent.ui.monitor.ScreenMirrorScreen
import com.nirapod.family.parent.ui.settings.SettingsScreen
import com.nirapod.family.parent.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Devices : Screen("devices")
    object License : Screen("license")
    object Settings : Screen("settings")
    object ScreenMirror : Screen("screen_mirror/{deviceId}") {
        fun createRoute(deviceId: String) = "screen_mirror/$deviceId"
    }
    object CameraView : Screen("camera_view/{deviceId}") {
        fun createRoute(deviceId: String) = "camera_view/$deviceId"
    }
    object AudioMonitor : Screen("audio_monitor/{deviceId}") {
        fun createRoute(deviceId: String) = "audio_monitor/$deviceId"
    }
    object LocationMap : Screen("location_map/{deviceId}") {
        fun createRoute(deviceId: String) = "location_map/$deviceId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToScreenMirror = { id -> navController.navigate(Screen.ScreenMirror.createRoute(id)) },
                onNavigateToCamera = { id -> navController.navigate(Screen.CameraView.createRoute(id)) },
                onNavigateToAudio = { id -> navController.navigate(Screen.AudioMonitor.createRoute(id)) },
                onNavigateToLocation = { id -> navController.navigate(Screen.LocationMap.createRoute(id)) },
                onNavigateToDevices = { navController.navigate(Screen.Devices.route) },
                onNavigateToLicense = { navController.navigate(Screen.License.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Devices.route) {
            DevicesScreen(
                onBack = { navController.popBackStack() },
                onDeviceSelect = { id -> navController.navigate(Screen.CameraView.createRoute(id)) }
            )
        }
        composable(Screen.License.route) {
            LicenseScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.ScreenMirror.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            ScreenMirrorScreen(deviceId = deviceId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.CameraView.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            CameraViewScreen(deviceId = deviceId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.AudioMonitor.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            AudioMonitorScreen(deviceId = deviceId, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.LocationMap.route,
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            LocationMapScreen(deviceId = deviceId, onBack = { navController.popBackStack() })
        }
    }
}
