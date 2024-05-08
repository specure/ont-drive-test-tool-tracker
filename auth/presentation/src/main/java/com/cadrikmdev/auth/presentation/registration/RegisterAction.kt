package com.cadrikmdev.auth.presentation.registration

sealed interface RegisterAction {
    data object OnTogglePasswordVisibilityClick : RegisterAction
    data object OnLoginClick : RegisterAction
    data object OnRegisterClick : RegisterAction
}