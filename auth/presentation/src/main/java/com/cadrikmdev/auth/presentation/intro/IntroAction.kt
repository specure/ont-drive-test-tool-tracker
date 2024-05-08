package com.cadrikmdev.auth.presentation.intro

sealed interface IntroAction {

    data object OnSingInClick : IntroAction
    data object OnSingUpClick : IntroAction
}