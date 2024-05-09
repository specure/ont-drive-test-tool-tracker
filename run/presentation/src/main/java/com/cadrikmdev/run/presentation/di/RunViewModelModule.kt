package com.cadrikmdev.run.presentation.di

import com.cadrikmdev.run.presentation.overview.RunOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val runViewModelModule = module {
    viewModelOf(::RunOverviewViewModel)
}