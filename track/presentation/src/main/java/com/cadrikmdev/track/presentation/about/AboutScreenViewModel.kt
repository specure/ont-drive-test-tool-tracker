package com.cadrikmdev.track.presentation.about

import android.content.Context
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel

class AboutScreenViewModel(
    context: Context
) : ViewModel() {
    val packageInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    val versionCode = packageInfo.longVersionCode
}