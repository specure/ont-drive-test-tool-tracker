package com.specure.updater.data.di

import com.specure.updater.data.BuildConfig
import com.specure.updater.data.GithubAppUpdater
import com.specure.updater.data.UpdaterClientFactory
import com.specure.updater.domain.Updater
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val DI_HTTP_JSON_CLIENT = "httpJsonClient"
const val DI_HTTP_DATA_CLIENT = "httpDataClient"

val updaterDataModule = module {
    single<HttpClient>(named(DI_HTTP_JSON_CLIENT)) {
        UpdaterClientFactory(BuildConfig.GITHUB_API_TOKEN).build()
    }

    single<HttpClient>(named(DI_HTTP_DATA_CLIENT)) {
        UpdaterClientFactory(BuildConfig.GITHUB_API_TOKEN).buildDownloadClient()
    }

    single<Updater> {
        GithubAppUpdater(
            get(named(DI_HTTP_DATA_CLIENT)),
            get(named(DI_HTTP_JSON_CLIENT)),
            get()
        )
    }
}