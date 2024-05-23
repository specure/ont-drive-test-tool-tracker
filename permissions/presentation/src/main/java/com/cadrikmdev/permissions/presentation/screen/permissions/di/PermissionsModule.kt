package com.cadrikmdev.permissions.presentation.screen.permissions.di

import com.cadrikmdev.permissions.presentation.screen.permissions.viewmodel.PermissionsScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val permissionsModule = module {
    viewModelOf(::PermissionsScreenViewModel)
}