package com.specure.updater.data.di

import com.specure.updater.data.BuildConfig
import com.specure.updater.data.GithubAppUpdater
import com.specure.updater.data.UpdaterClientFactory
import com.specure.updater.domain.Updater
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val updaterDataModule = module {
    single {
        UpdaterClientFactory(BuildConfig.GITHUB_API_TOKEN).build()
    }

    singleOf(::GithubAppUpdater).bind<Updater>()
}