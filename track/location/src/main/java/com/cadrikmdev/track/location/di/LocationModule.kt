package com.cadrikmdev.track.location.di

import com.cadrikmdev.track.domain.LocationObserver
import com.cadrikmdev.track.location.AndroidLocationObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val locationModule = module {
    singleOf(::AndroidLocationObserver).bind<LocationObserver>()
}