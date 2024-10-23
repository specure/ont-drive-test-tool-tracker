package com.specure.updater.data.test


import com.specure.updater.data.BuildConfig
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Streaming
import retrofit2.http.Url

interface IGithubApi {
    @Headers(
        "Accept: application/vnd.github+json",
        "X-GitHub-Api-Version: 2022-11-28",
        "Authorization: Bearer ${BuildConfig.GITHUB_API_TOKEN}"
    )
    @GET("releases/latest")
    fun getLatestRelease(): Call<GithubRelease>

    @Streaming
    @Headers(
        "Accept: application/octet-stream",
        "X-GitHub-Api-Version: 2022-11-28",
        "Authorization: Bearer ${BuildConfig.GITHUB_API_TOKEN}"
    )
    @GET
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>
}