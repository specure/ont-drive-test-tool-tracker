package com.cadrikmdev.analytics.presentation.di

import com.cadrikmdev.analytics.presentation.AnalyticsDashboardViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val analyticsViewModelModule = module {
    viewModelOf(::AnalyticsDashboardViewModel)
}