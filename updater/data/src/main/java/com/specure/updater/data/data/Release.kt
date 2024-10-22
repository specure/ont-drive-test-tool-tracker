package com.specure.updater.data.data

import kotlinx.serialization.Serializable

@Serializable
data class Release(
    val tag_name: String,
    val assets: List<Asset>
)
