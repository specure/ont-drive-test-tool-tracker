package com.cadrikmdev.presentation.intro

sealed interface IntroAction {

    data object OnSingInClick : IntroAction
    data object OnSingUpClick : IntroAction
}