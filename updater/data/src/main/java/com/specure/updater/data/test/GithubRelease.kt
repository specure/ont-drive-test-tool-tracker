package com.specure.updater.data.test

import com.google.gson.annotations.SerializedName

data class GithubRelease(
    val assets: List<GithubReleaseAsset>,
    val draft: Boolean,
    val name: String,
    val prerelease: Boolean,
    @SerializedName("tag_name") val tagName: String,
)
