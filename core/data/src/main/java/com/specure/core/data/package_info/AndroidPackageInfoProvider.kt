package com.specure.core.data.package_info

import android.content.Context
import android.content.pm.PackageInfo
import com.specure.core.domain.package_info.PackageInfoProvider

class AndroidPackageInfoProvider(
    appContext: Context
) : PackageInfoProvider {
    private val packageInfo: PackageInfo =
        appContext.packageManager.getPackageInfo(appContext.packageName, 0)

    override val versionName: String = packageInfo.versionName

    override val versionCode = packageInfo.longVersionCode
}