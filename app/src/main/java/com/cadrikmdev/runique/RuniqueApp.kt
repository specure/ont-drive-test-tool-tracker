package com.cadrikmdev.runique

import android.app.Application
import android.content.Context
import com.cadrikmdev.auth.data.di.authDataModule
import com.cadrikmdev.auth.presentation.di.authViewModelModule
import com.cadrikmdev.core.data.di.coreDataModule
import com.cadrikmdev.core.database.di.databaseModule
import com.cadrikmdev.run.data.di.runDataModule
import com.cadrikmdev.run.location.di.locationModule
import com.cadrikmdev.run.network.di.runNetworkModule
import com.cadrikmdev.run.presentation.di.runPresentationModule
import com.cadrikmdev.runique.di.appModule
import com.google.android.play.core.splitcompat.SplitCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class RuniqueApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {

        val applicationScope = CoroutineScope(SupervisorJob())

        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidLogger()
            androidContext(this@RuniqueApp)
            workManagerFactory()
            modules(
                appModule,
                authDataModule,
                authViewModelModule,
                coreDataModule,
                databaseModule,
                runDataModule,
                runNetworkModule,
                runPresentationModule,
                locationModule,
            )
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        SplitCompat.install(this)
    }
}