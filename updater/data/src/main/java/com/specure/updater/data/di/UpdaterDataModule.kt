package com.specure.updater.data.di

import com.specure.updater.data.BuildConfig
import com.specure.updater.data.GithubAppUpdater
import com.specure.updater.data.UpdaterClientFactory
import com.specure.updater.domain.Updater
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val updaterDataModule = module {
    single<HttpClient>(named("jsonClient")) {
        UpdaterClientFactory(BuildConfig.GITHUB_API_TOKEN).build()
    }

    single<HttpClient>(named("downloadClient")) {
        UpdaterClientFactory(BuildConfig.GITHUB_API_TOKEN).buildDownloadClient()
    }

    single<Updater> {
        GithubAppUpdater(
            get(named("downloadClient")),
            get(named("jsonClient")),
            get()
        )
    }
}