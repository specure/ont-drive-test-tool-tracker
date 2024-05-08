package com.cadrikmdev.core.data.di

import com.cadrikmdev.core.data.auth.EncryptedSessionStorage
import com.cadrikmdev.core.data.networking.HttpClientFactory
import com.cadrikmdev.core.domain.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory(get()).build()
    }

    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()
}