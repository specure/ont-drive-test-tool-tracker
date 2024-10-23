package com.specure.updater.domain

import kotlinx.coroutines.flow.SharedFlow

interface Updater {

    val updateStatus: SharedFlow<UpdatingStatus>

    suspend fun checkForUpdate()

    suspend fun downloadAndInstallUpdate()

    suspend fun checkAndInstall()
}
