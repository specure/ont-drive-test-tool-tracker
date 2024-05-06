package com.cadrikmdev.core.data.di

import com.cadrikmdev.core.data.networking.HttpClientFactory
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory().build()
    }
}