package com.cadrikmdev.permissions.presentation.model

import android.os.Build
import com.cadrikmdev.permissions.domain.model.Permission

data class AndroidPermission(
    override val name: String,
    override val minimumApiRequired: Int = 0,
    override val maximumApiRequired: Int = Int.MAX_VALUE, // (e.g. WRITE_EXTERNAL_STORAGE is not needed on api level 29 anymore)
    override val dependsOn: String? = null, // if you are not allowed to ask for permission before another permission is granted, e.g. location background permission you are allowed to ask only AFTER location permission is granted so here you can put coarse_location_permission
    override var isGranted: Boolean = minimumApiRequired > Build.VERSION.SDK_INT,
    override var isAsked: Boolean = false,
    override val isTargetApiToBeAsked: Boolean = Build.VERSION.SDK_INT in minimumApiRequired..maximumApiRequired,
    override var shouldShowRationale: Boolean = false,
    override var shouldCheckShouldShowRationale: Boolean = false,
) : Permission()