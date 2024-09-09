package com.cadrikmdev.signaltracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.cadrikmdev.permissions.presentation.screen.permissions.PermissionsScreen
import com.cadrikmdev.permissions.presentation.util.openAppSettings
import com.cadrikmdev.signaltracker.util.extensions.startTrackingService
import com.cadrikmdev.signaltracker.util.extensions.stopTrackingService
import com.cadrikmdev.track.presentation.about.AboutScreenNav
import com.cadrikmdev.track.presentation.about.AboutScreenRoot
import com.cadrikmdev.track.presentation.active_track.ActiveTrackScreenRoot
import com.cadrikmdev.track.presentation.settings.SettingsScreenRoot
import com.cadrikmdev.track.presentation.settings.navigation.SettingsScreenNav
import com.cadrikmdev.track.presentation.track_overview.TrackOverviewScreenRoot

@Composable
fun NavigationRoot(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = "track"
    ) {
        trackGraph(
            navController = navController,
        )
        permissionsGraph(
            navController = navController,
        )
    }
}

private fun NavGraphBuilder.trackGraph(
    navController: NavHostController,
) {
    navigation(
        startDestination = "track_overview",
        route = "track"
    ) {
        composable("track_overview") {
            TrackOverviewScreenRoot(
                onStartTrackClick = {
                    navController.navigate("active_track")
                },
                onSettingsClick = {
                    navController.navigate(SettingsScreenNav)
                },
                onAboutClick = {
                    navController.navigate(AboutScreenNav)
                },
                onResolvePermissionClick = {
                    navController.navigate("permissions")
                }
            )
        }
        composable(
            route = "active_track",
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "signaltracker://active_track"
                }
            )
        ) {
            val context = LocalContext.current
            ActiveTrackScreenRoot(
                onBack = {
                    navController.navigateUp()
                },
                onFinish = {
                    navController.navigateUp()
                },
                onServiceToggle = { shouldServiceRun ->
                    if (shouldServiceRun) {
                        context.startTrackingService()
                    } else {
                        context.stopTrackingService()
                    }
                }
            )
        }
        composable<SettingsScreenNav> {
            SettingsScreenRoot(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
        composable<AboutScreenNav> {
            AboutScreenRoot(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}

private fun NavGraphBuilder.permissionsGraph(
    navController: NavHostController,
) {
    navigation(
        startDestination = "permissions_screen",
        route = "permissions"
    ) {
        composable("permissions_screen") {
            val context = LocalContext.current
            PermissionsScreen(
                onBackPressed = {
                    navController.navigate("track_overview") {
                        popUpTo("permissions") {
                            inclusive = true
                        }
                    }
                },
                openAppSettings = {
                    context.openAppSettings()
                }
            )
        }
    }
}