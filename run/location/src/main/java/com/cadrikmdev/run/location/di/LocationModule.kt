package com.cadrikmdev.run.location.di

import com.cadrikmdev.run.domain.LocationObserver
import com.cadrikmdev.run.location.AndroidLocationObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val locationModule = module {
    singleOf(::AndroidLocationObserver).bind<LocationObserver>()
}