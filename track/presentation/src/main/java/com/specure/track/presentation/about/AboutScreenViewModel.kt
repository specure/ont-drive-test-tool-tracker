package com.specure.track.presentation.about

import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.specure.updater.domain.Updater
import com.specure.updater.domain.UpdatingStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AboutScreenViewModel(
    context: Context,
    private val updater: Updater
) : ViewModel() {

    val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    val versionCode = packageInfo.longVersionCode

    var updateState = updater.updateStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UpdatingStatus.Idle
    )

    fun onAction(action: AboutScreenAction) {
        when (action) {
            AboutScreenAction.OnCheckUpdateClick -> {
                viewModelScope.launch {
                    updater.checkForUpdate()
                }
            }

            AboutScreenAction.OnInstallUpdateClick -> {
                viewModelScope.launch {
                    updater.installUpdate()
                }
            }
        }
    }


}