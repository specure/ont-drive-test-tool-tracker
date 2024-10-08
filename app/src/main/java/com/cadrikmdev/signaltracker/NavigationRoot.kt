package com.cadrikmdev.signaltracker

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
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
            val context = LocalContext.current
            SettingsScreenRoot(
                onBackClick = {
                    navController.navigateUp()
                },
                onOpenRadioSettingsClick = {
                    openRadioInfoActivity(context)
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

private fun openRadioInfoActivity(context: Context) {
    try {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val radioInfoComponentName =
                ComponentName("com.android.phone", "com.android.phone.settings.RadioInfo")
            Intent().apply {
                component = radioInfoComponentName
            }
        } else {
            val i = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                i.setClassName("com.android.phone", "com.android.phone.settings.RadioInfo")
            } else {
                i.setClassName("com.android.settings", "com.android.settings.RadioInfo")
            }
        }
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        showNotSupportedToast(context)
    } catch (e: SecurityException) {
        showNotSupportedToast(context)
    }
}

private fun showNotSupportedToast(context: Context) {
    Toast.makeText(
        context,
        com.cadrikmdev.track.presentation.R.string.open_device_radio_settings_error,
        Toast.LENGTH_SHORT
    ).show()
}