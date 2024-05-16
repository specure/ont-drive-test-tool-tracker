package com.cadrikmdev.analytics.presentation

sealed interface AnalyticsAction {
    data object OnBackClick : AnalyticsAction
}