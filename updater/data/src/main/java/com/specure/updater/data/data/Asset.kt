package com.specure.updater.data.data

import kotlinx.serialization.Serializable

@Serializable
data class Asset(
    val name: String,
    val browser_download_url: String
)
