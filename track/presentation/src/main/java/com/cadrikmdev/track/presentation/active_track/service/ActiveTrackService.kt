package com.cadrikmdev.track.presentation.active_track.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.cadrikmdev.core.presentation.ui.formatted
import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import kotlin.time.Duration


class ActiveTrackService : Service() {

    private val notificationManager by lazy {
        getSystemService<NotificationManager>()!!
    }

    private lateinit var wakeLock: PowerManager.WakeLock


    private val baseNotification by lazy {
        NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(com.cadrikmdev.core.presentation.designsystem.R.drawable.logo)
            .setContentTitle(getString(R.string.active_track))
            .setOnlyAlertOnce(true)
    }

    private val measurementTracker: MeasurementTracker by inject<MeasurementTracker>()

    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, WAKE_LOG_TAG)

        // Acquire the wake lock
        wakeLock.acquire()
    }

    override fun onDestroy() {
        wakeLock.release()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityClass = intent.getStringExtra(EXTRA_ACTIVITY_CLASS)
                    ?: throw IllegalArgumentException("No activity class provided")
                start(Class.forName(activityClass))
            }

            ACTION_STOP -> {
                stop()
            }
        }
        return START_STICKY
    }

    private fun start(activityClass: Class<*>) {
        if (!isServiceActive) {
            isServiceActive = true
            createNotificationChannel()

            val activityIntent = Intent(applicationContext, activityClass).apply {
                data = "signaltracker://active_track".toUri() // specified deeplink
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

            val pendingIntent = TaskStackBuilder.create(applicationContext).run {
                addNextIntentWithParentStack(activityIntent)
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            }
            val notification = baseNotification
                .setContentText("00:00:00")
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)
            updateNotification()
        }
    }

    private fun updateNotification() {
        var previousElapsedTime = Duration.ZERO
        measurementTracker.elapsedTime.onEach { elapsedTime ->
            if (elapsedTime.inWholeSeconds > previousElapsedTime.inWholeSeconds) {
                val notification = baseNotification
                    .setContentText(elapsedTime.formatted())
                    .apply {
                        priority = NotificationCompat.PRIORITY_MIN
                    }
                    .setSilent(true)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true)
                    .build()
                notificationManager.notify(1, notification)
                previousElapsedTime = elapsedTime
            }
        }.launchIn(serviceScope)
    }

    fun stop() {
        stopSelf()
        isServiceActive = false
        serviceScope.cancel()

        serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.active_track),
                NotificationManager.IMPORTANCE_NONE
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        var isServiceActive = false
        private const val CHANNEL_ID = "active_track"

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        const val WAKE_LOG_TAG = "SIGNAL_TRACKER:WAKE_LOCK_TAG"


        private const val EXTRA_ACTIVITY_CLASS = "EXTRA_ACTIVITY_CLASS"

        fun createStartIntent(context: Context, activityClass: Class<*>): Intent {
            return Intent(context, ActiveTrackService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACTIVITY_CLASS, activityClass.name)
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, ActiveTrackService::class.java).apply {
                action = ACTION_STOP
            }
        }

    }
}