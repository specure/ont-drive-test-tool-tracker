package com.cadrikmdev.run.network.di

import com.cadrikmdev.core.domain.run.RemoteRunDataSource
import com.cadrikmdev.run.network.KtorRemoteRunDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val runNetworkModule = module {
    singleOf(::KtorRemoteRunDataSource).bind<RemoteRunDataSource>()
}