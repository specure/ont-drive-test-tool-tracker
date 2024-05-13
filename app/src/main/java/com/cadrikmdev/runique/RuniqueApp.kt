package com.cadrikmdev.runique

import android.app.Application
import com.cadrikmdev.auth.data.di.authDataModule
import com.cadrikmdev.auth.presentation.di.authViewModelModule
import com.cadrikmdev.core.data.di.coreDataModule
import com.cadrikmdev.run.location.di.locationModule
import com.cadrikmdev.run.presentation.di.runViewModelModule
import com.cadrikmdev.runique.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
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
            modules(
                appModule,
                authDataModule,
                authViewModelModule,
                coreDataModule,
                runViewModelModule,
                locationModule,
            )
        }
    }
}