package com.cadrikmdev.permissions.presentation.di

import com.cadrikmdev.permissions.domain.PermissionHandler
import com.cadrikmdev.permissions.presentation.screen.permissions.PermissionHandlerImpl
import com.cadrikmdev.permissions.presentation.screen.permissions.PermissionsScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val permissionsModule = module {
    viewModelOf(::PermissionsScreenViewModel)

    singleOf(::PermissionHandlerImpl).bind<PermissionHandler>()
}
