package com.cadrikmdev.presentation.di

import com.cadrikmdev.presentation.registration.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val authViewModelModule = module {
    viewModelOf(::RegisterViewModel)
}