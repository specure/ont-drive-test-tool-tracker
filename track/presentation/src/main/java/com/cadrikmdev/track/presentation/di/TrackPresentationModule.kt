package com.cadrikmdev.track.presentation.di

import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.active_track.ActiveTrackViewModel
import com.cadrikmdev.track.presentation.track_overview.TrackOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val trackPresentationModule = module {
    viewModelOf(::TrackOverviewViewModel)
    viewModelOf(::ActiveTrackViewModel)
    singleOf(::MeasurementTracker)
}