package com.cadrikmdev.track.presentation.di

import com.cadrikmdev.core.domain.location.service.LocationServiceObserver
import com.cadrikmdev.core.domain.track.TemperatureInfoObserver
import com.cadrikmdev.core.domain.wifi.WifiServiceObserver
import com.cadrikmdev.core.presentation.service.ServiceChecker
import com.cadrikmdev.core.presentation.service.location.AndroidLocationServiceObserver
import com.cadrikmdev.core.presentation.service.location.GpsLocationServiceChecker
import com.cadrikmdev.core.presentation.service.temperature.TemperatureInfoReceiver
import com.cadrikmdev.core.presentation.service.wifi.AndroidWifiServiceObserver
import com.cadrikmdev.iperf.domain.IperfOutputParser
import com.cadrikmdev.iperf.domain.IperfRunner
import com.cadrikmdev.iperf.presentation.IperfAndroidParser
import com.cadrikmdev.iperf.presentation.IperfDownloadRunner
import com.cadrikmdev.iperf.presentation.IperfUploadRunner
import com.cadrikmdev.track.domain.MeasurementTracker
import com.cadrikmdev.track.presentation.active_track.ActiveTrackViewModel
import com.cadrikmdev.track.presentation.settings.SettingsScreenViewModel
import com.cadrikmdev.track.presentation.track_overview.TrackOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val trackPresentationModule = module {
    viewModelOf(::TrackOverviewViewModel)
    viewModelOf(::ActiveTrackViewModel)
    viewModelOf(::SettingsScreenViewModel)

    singleOf(::GpsLocationServiceChecker).bind<ServiceChecker>()
    singleOf(::AndroidLocationServiceObserver).bind<LocationServiceObserver>()
    singleOf(::TemperatureInfoReceiver).bind<TemperatureInfoObserver>()
    singleOf(::AndroidWifiServiceObserver).bind<WifiServiceObserver>()
    singleOf(::IperfAndroidParser).bind<IperfOutputParser>()
    single<IperfRunner>(named("iperfDownloadRunner")) {
        IperfDownloadRunner(
            get(),
            get(),
            get(),
        )
    }
    single<IperfRunner>(named("iperfUploadRunner")) {
        IperfUploadRunner(
            get(),
            get(),
            get(),
        )
    }
    single {
        MeasurementTracker(
            get(),
            get(),
            get(),
            get(),
            get(named("iperfDownloadRunner")),
            get(named("iperfUploadRunner")),
            get(),
            get(),
            get(),
        )
    }
}