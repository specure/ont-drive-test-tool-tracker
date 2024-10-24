package com.specure.core.data.di

import com.specure.core.data.auth.EncryptedSessionStorage
import com.specure.core.data.networking.HttpClientFactory
import com.specure.core.data.package_info.AndroidPackageInfoProvider
import com.specure.core.data.track.OfflineFirstTrackRepository
import com.specure.core.domain.SessionStorage
import com.specure.core.domain.package_info.PackageInfoProvider
import com.specure.core.domain.track.TrackRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    single {
        HttpClientFactory(get()).build()
    }

    singleOf(::EncryptedSessionStorage).bind<SessionStorage>()

    singleOf(::OfflineFirstTrackRepository).bind<TrackRepository>()

    singleOf(::AndroidPackageInfoProvider).bind<PackageInfoProvider>()
}