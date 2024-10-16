package com.specure.track.location.di

import com.specure.track.domain.LocationObserver
import com.specure.track.location.AndroidLocationObserver
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val locationModule = module {
    singleOf(::AndroidLocationObserver).bind<LocationObserver>()
}