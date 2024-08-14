package com.cadrikmdev.signaltracker.util.extensions

import android.content.Context
import com.cadrikmdev.signaltracker.MainActivity
import com.cadrikmdev.track.presentation.active_track.service.ActiveTrackService

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