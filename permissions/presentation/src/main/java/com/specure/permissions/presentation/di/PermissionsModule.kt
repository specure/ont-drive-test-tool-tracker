package com.specure.permissions.presentation.di

import com.specure.permissions.domain.PermissionHandler
import com.specure.permissions.presentation.screen.permissions.PermissionHandlerImpl
import com.specure.permissions.presentation.screen.permissions.PermissionsScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val permissionsModule = module {
    viewModelOf(::PermissionsScreenViewModel)

    singleOf(::PermissionHandlerImpl).bind<PermissionHandler>()
}
