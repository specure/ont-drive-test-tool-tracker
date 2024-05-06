package com.cadrikmdev.runique

import android.app.Application
import com.cadrikmdev.auth.data.di.authDataModule
import com.cadrikmdev.core.data.di.coreDataModule
import com.cadrikmdev.presentation.di.authViewModelModule
import com.cadrikmdev.runique.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class RuniqueApp : Application() {

    override fun onCreate() {
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
            )
        }
    }
}