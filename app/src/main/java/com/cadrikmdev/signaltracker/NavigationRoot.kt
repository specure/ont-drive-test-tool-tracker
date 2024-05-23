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
import com.cadrikmdev.track.presentation.active_track.ActiveTrackScreenRoot
import com.cadrikmdev.track.presentation.active_track.service.ActiveTrackService
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
                        context.startService(
                            ActiveTrackService.createStartIntent(
                                context = context,
                                activityClass = MainActivity::class.java
                            )
                        )
                    } else {
                        context.startService(
                            ActiveTrackService.createStopIntent(
                                context = context,
                            )
                        )
                    }
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
                    navController.navigateUp()
                },
                openAppSettings = {
                    context.openAppSettings()
                }
            )
        }
    }
}