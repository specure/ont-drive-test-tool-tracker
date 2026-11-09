package com.specure.signaltracker.util.extensions

import android.content.Context
import com.specure.signaltracker.MainActivity
import com.specure.track.presentation.screen.active_track.service.ActiveTrackService

fun Context.startTrackingService() {
    this.startService(
        ActiveTrackService.createStartIntent(
            context = this,
            activityClass = MainActivity::class.java
        )
    )
}

fun Context.stopTrackingService() {
    this.startService(
        ActiveTrackService.createStopIntent(
            context = this,
        )
    )
}