package com.cadrikmdev.track.presentation.active_track

sealed interface ActiveTrackAction {
    data object OnToggleTrackClick : ActiveTrackAction
    data object OnFinishTrackClick : ActiveTrackAction
    data object OnResumeTrackClick : ActiveTrackAction
    data object OnBackClick : ActiveTrackAction

    data class SubmitLocationPermissionInfo(
        val acceptedLocationPermission: Boolean,
        val showLocationRationale: Boolean
    ) : ActiveTrackAction

    data class SubmitNotificationPermissionInfo(
        val acceptedNotificationPermission: Boolean,
        val showNotificationRationale: Boolean
    ) : ActiveTrackAction

    data object DismissRationaleDialog : ActiveTrackAction
    data object OnStopTrackClick : ActiveTrackAction
    data object OnStartTrackClick : ActiveTrackAction

    class OnTrackProcessed(val mapPictureBytes: ByteArray) : ActiveTrackAction
}