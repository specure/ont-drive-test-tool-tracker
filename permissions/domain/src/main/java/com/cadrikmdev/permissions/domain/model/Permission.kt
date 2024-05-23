package com.cadrikmdev.permissions.domain.model

abstract class Permission {
    abstract val name: String
    abstract val minimumApiRequired: Int
    abstract val maximumApiRequired: Int
    abstract val dependsOn: String? // if you are not allowed to ask for permission before another permission is granted, e.g. location background permission you are allowed to ask only AFTER location permission is granted so here you can put coarse_location_permission
    abstract var isGranted: Boolean
    abstract var isAsked: Boolean
    abstract val isTargetApiToBeAsked: Boolean
    abstract var shouldShowRationale: Boolean
    abstract var shouldCheckShouldShowRationale: Boolean
}
