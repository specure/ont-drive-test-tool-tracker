package com.specure.track.presentation.screen.about

sealed interface AboutScreenAction {
    data object OnCheckUpdateClick : AboutScreenAction
    data object OnInstallUpdateClick : AboutScreenAction
}