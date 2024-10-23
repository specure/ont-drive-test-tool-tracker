package com.specure.updater.domain

sealed class UpdatingStatus {
    data object Idle : UpdatingStatus()
    data object Checking : UpdatingStatus()
    data object NoNewVersion : UpdatingStatus()
    data class NewVersionFound(val version: String) : UpdatingStatus()
    data object Downloading : UpdatingStatus()
    data object InstallingInteractive : UpdatingStatus()
    data object InstallingSilently : UpdatingStatus()
    data class Error(val message: String) : UpdatingStatus()
    data class ErrorCheckingUpdate(val message: String?) : UpdatingStatus()
    data class ErrorDownloading(val message: String?) : UpdatingStatus()
}