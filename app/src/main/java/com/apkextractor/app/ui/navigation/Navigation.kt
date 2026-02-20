package com.apkextractor.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.apkextractor.app.ui.screens.about.AboutScreen
import com.apkextractor.app.ui.screens.appdetail.AppDetailScreen
import com.apkextractor.app.ui.screens.applist.AppListScreen
import com.apkextractor.app.ui.screens.settings.SettingsScreen

@Composable
fun ApkExtractorNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "app_list") {
        composable("app_list") {
            AppListScreen(
                onAppClick = { packageName ->
                    navController.navigate("app_detail/$packageName")
                },
                onSettingsClick = { navController.navigate("settings") },
                onAboutClick = { navController.navigate("about") }
            )
        }

        composable(
            route = "app_detail/{packageName}",
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            AppDetailScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable("about") {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
