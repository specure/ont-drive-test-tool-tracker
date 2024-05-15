package com.cadrikmdev.core.data.di

import com.cadrikmdev.core.data.auth.EncryptedSessionStorage
import com.cadrikmdev.core.data.networking.HttpClientFactory
import com.cadrikmdev.core.data.run.OfflineFirstRunRepository
import com.cadrikmdev.core.domain.SessionStorage
import com.cadrikmdev.core.domain.run.RunRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory(get()).build()
    }

    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()

    singleOf(::OfflineFirstRunRepository).bind<RunRepository>()
}