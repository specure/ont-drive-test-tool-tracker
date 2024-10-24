package com.specure.track.presentation.di

import com.specure.core.domain.config.Config
import com.specure.core.domain.location.service.LocationServiceObserver
import com.specure.core.domain.track.TemperatureInfoObserver
import com.specure.core.domain.wifi.WifiServiceObserver
import com.specure.core.presentation.AppConfig
import com.specure.core.presentation.service.ServiceChecker
import com.specure.core.presentation.service.location.AndroidLocationServiceObserver
import com.specure.core.presentation.service.location.GpsLocationServiceChecker
import com.specure.core.presentation.service.temperature.TemperatureInfoReceiver
import com.specure.core.presentation.service.wifi.AndroidWifiServiceObserver
import com.specure.iperf.domain.IperfOutputParser
import com.specure.iperf.domain.IperfRunner
import com.specure.iperf.presentation.IperfAndroidParser
import com.specure.iperf.presentation.IperfDownloadRunner
import com.specure.iperf.presentation.IperfUploadRunner
import com.specure.track.domain.MeasurementTracker
import com.specure.track.presentation.about.AboutScreenViewModel
import com.specure.track.presentation.active_track.ActiveTrackViewModel
import com.specure.track.presentation.settings.SettingsScreenStateManager
import com.specure.track.presentation.settings.SettingsScreenViewModel
import com.specure.track.presentation.track_overview.TrackOverviewViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val trackPresentationModule = module {
    viewModelOf(::AboutScreenViewModel)
    viewModelOf(::ActiveTrackViewModel)
    viewModelOf(::SettingsScreenViewModel)
    viewModelOf(::TrackOverviewViewModel)

    singleOf(::GpsLocationServiceChecker).bind<ServiceChecker>()
    singleOf(::AndroidLocationServiceObserver).bind<LocationServiceObserver>()
    singleOf(::TemperatureInfoReceiver).bind<TemperatureInfoObserver>()
    singleOf(::AndroidWifiServiceObserver).bind<WifiServiceObserver>()
    singleOf(::IperfAndroidParser).bind<IperfOutputParser>()
    singleOf(::AppConfig).bind<Config>()
    single<IperfRunner>(named("iperfDownloadRunner")) {
        IperfDownloadRunner(
            get(),
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
            get(),
            get(),
        )
    }

    singleOf(::SettingsScreenStateManager)

}