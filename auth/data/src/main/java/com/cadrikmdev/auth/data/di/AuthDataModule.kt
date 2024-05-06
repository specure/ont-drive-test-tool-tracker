package com.cadrikmdev.auth.data.di

import com.cadrikmdev.auth.data.EmailPatternValidator
import com.cadrikmdev.auth.domain.PatternValidator
import com.cadrikmdev.auth.domain.UserDataValidator
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val authDataModule = module {
    single<PatternValidator> {
        EmailPatternValidator
    }
    singleOf(::UserDataValidator)
}