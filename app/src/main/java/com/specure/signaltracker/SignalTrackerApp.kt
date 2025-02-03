package com.specure.signaltracker

import android.app.Application
import com.specure.connectivity.presentation.mobile_network.di.mobileNetworkModule
import com.specure.connectivity.presentation.network.di.connectivityModule
import com.specure.core.data.di.coreDataModule
import com.specure.core.database.di.databaseModule
import com.specure.permissions.presentation.di.permissionsModule
import com.specure.signaltracker.di.appIntercomModule
import com.specure.signaltracker.di.appModule
import com.specure.track.data.di.trackDataModule
import com.specure.track.location.di.locationModule
import com.specure.track.presentation.di.trackPresentationModule
import com.specure.updater.data.di.updaterDataModule
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
        if (com.specure.signaltracker.BuildConfig.DEBUG) {
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
                appIntercomModule,
                mobileNetworkModule,
                permissionsModule,
                trackDataModule,
                trackPresentationModule,
                locationModule,
                updaterDataModule,
            )
        }
    }
}