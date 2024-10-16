package com.specure.signaltracker.util.extensions

import android.content.Context
import com.specure.track.presentation.active_track.service.ActiveTrackService

fun Context.startTrackingService() {
    this.startService(
        ActiveTrackService.createStartIntent(
            context = this,
            activityClass = com.specure.signaltracker.MainActivity::class.java
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