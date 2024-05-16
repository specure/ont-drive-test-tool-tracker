package com.cadrikmdev.analytics.data.di

import com.cadrikmdev.analytics.data.RoomAnalyticsRepository
import com.cadrikmdev.analytics.domain.AnalyticsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val analyticsDataModule = module {
    singleOf(::RoomAnalyticsRepository).bind<AnalyticsRepository>()
}