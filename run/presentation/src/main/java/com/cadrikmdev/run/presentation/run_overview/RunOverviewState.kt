package com.cadrikmdev.run.presentation.run_overview

import com.cadrikmdev.run.presentation.run_overview.model.RunUi

data class RunOverviewState(
    val runs: List<RunUi> = emptyList(),
)
