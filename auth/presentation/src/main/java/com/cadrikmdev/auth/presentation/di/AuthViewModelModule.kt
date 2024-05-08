package com.cadrikmdev.auth.presentation.di

import com.cadrikmdev.auth.presentation.login.LoginViewModel
import com.cadrikmdev.auth.presentation.registration.RegisterViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val authViewModelModule = module {
    viewModelOf(::RegisterViewModel)
    viewModelOf(::LoginViewModel)
}