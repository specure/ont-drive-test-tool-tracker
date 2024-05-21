package com.cadrikmdev.track.network.di

import com.cadrikmdev.core.domain.track.RemoteTrackDataSource
import com.cadrikmdev.track.network.KtorRemoteTrackDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val trackNetworkModule = module {
    singleOf(::KtorRemoteTrackDataSource).bind<RemoteTrackDataSource>()
}