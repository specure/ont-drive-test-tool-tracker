package com.cadrikmdev.signaltracker

import android.app.Application
import com.cadrikmdev.connectivity.presentation.mobile_network.di.mobileNetworkModule
import com.cadrikmdev.connectivity.presentation.network.di.connectivityModule
import com.cadrikmdev.core.data.di.coreDataModule
import com.cadrikmdev.core.database.di.databaseModule
import com.cadrikmdev.intercom.data.di.intercomDataModule
import com.cadrikmdev.permissions.presentation.di.permissionsModule
import com.cadrikmdev.signaltracker.di.appModule
import com.cadrikmdev.track.data.di.trackDataModule
import com.cadrikmdev.track.location.di.locationModule
import com.cadrikmdev.track.presentation.di.trackPresentationModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class SignalTrackerApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {

        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@SignalTrackerApp)
            workManagerFactory()
            modules(
                appModule,
                connectivityModule,
                coreDataModule,
                databaseModule,
                intercomDataModule,
                mobileNetworkModule,
                permissionsModule,
                trackDataModule,
                trackPresentationModule,
                locationModule,
            )
        }
    }
}