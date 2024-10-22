package com.specure.updater.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.specure.updater.data.data.Release
import com.specure.updater.domain.Updater
import com.specure.updater.domain.UpdatingStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import java.io.File

class GithubAppUpdater(
//    private val basicClient: HttpClient,
    private val jsonClient: HttpClient,
    private val appContext: Context,
) : Updater {

    private val _progressState = MutableSharedFlow<UpdatingStatus>()
    override val updateStatus: SharedFlow<UpdatingStatus> = _progressState

    private var updateFile: File? = null
    private var versionName: String? = null
    private var downloadUrl: String? = null

    override suspend fun checkForUpdate() {
        clearState()
        _progressState.emit(UpdatingStatus.Checking)
        val latestRelease = fetchLatestRelease()
        Timber.d("Latest release found: $latestRelease")
        latestRelease?.let { release ->
            val apkAsset = release.assets.find { it.name.endsWith(".apk") }
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            apkAsset?.let { apk ->
                val repoVersion = release.tag_name
                val latestVersion = getVersionToInstall(repoVersion, packageInfo.versionName)
                Timber.d("Latest release version: ${release.tag_name} vs. installed: ${packageInfo.versionName} - install: $latestVersion")
                if (repoVersion == latestVersion) {
                    versionName = latestVersion
                    downloadUrl = apk.browser_download_url
                    Timber.d("Latest release version to update to: ${release.tag_name}")
                    _progressState.emit(UpdatingStatus.NewVersionFound(latestVersion))
                    updateFile = File(appContext.getExternalFilesDir(null), apk.name)
                    return
                }
            }
        }
        _progressState.emit(UpdatingStatus.NoNewVersion)
    }

    override suspend fun installUpdate() {
        if (downloadUrl.isNullOrEmpty() || updateFile == null) {
            _progressState.emit(UpdatingStatus.DownloadFailed)
        }
        downloadUrl?.let { fileDownloadUrl ->
            updateFile?.let { file ->
                downloadApk(fileDownloadUrl, file)
                installApk(appContext, file)
                return
            }
        }
        _progressState.emit(UpdatingStatus.DownloadFailed)
    }

    private suspend fun fetchLatestRelease(): Release? {
        return try {
            jsonClient.get("${BuildConfig.GITHUB_API_REPO_URL}/releases/latest").body<Release>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun clearState() {
        updateFile = null
        versionName = null
        downloadUrl = null
    }

    suspend fun downloadApk(downloadUrl: String, outputFile: File) {
        _progressState.emit(UpdatingStatus.Downloading)

//        val response: HttpResponse = client.get(downloadUrl)
//        response.content.copyTo(outputFile.outputStream())
        val response = jsonClient.get(downloadUrl) {

            // Remove Content-Type if set globally
            headers.remove(HttpHeaders.ContentType)
            headers.remove(HttpHeaders.Accept)

            // Optionally set the Accept header to expect binary data
            header(HttpHeaders.Accept, ContentType.Application.OctetStream)
            header(HttpHeaders.ContentType, ContentType.Application.OctetStream)
        }

        outputFile.outputStream().use { fileStream ->
            val byteChannel: ByteReadChannel = response.bodyAsChannel()
//            val byteChannel: ByteReadChannel = response.content
            val buffer = ByteArray(1024)
            while (!byteChannel.isClosedForRead) {
                val bytesRead = byteChannel.readAvailable(buffer, 0, buffer.size)
                if (bytesRead > 0) {
                    fileStream.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private suspend fun installApk(context: Context, apkFile: File) {
        _progressState.emit(UpdatingStatus.Installing)
        val apkUri: Uri =
            FileProvider.getUriForFile(context, "${context.packageName}.updater.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(intent)
    }

    private fun getVersionToInstall(version1: String?, version2: String?): String? {
        if (version1 == null || version2 == null) {
            return null
        }
        val version1Parts = version1.split(".")
        val version2Parts = version2.split(".")
        if (version1Parts.size != version2Parts.size) {
            // manage here if you change version numbering in some way that it will contain more or less parts
            throw IllegalArgumentException("Version strings have a different length")
        }
        version1Parts.zip(version2Parts) { first, second ->
            when {
                (first > second) -> return version1
                (first < second) -> return version2
                else -> Unit
            }
        }
        return null
    }


//    fun installApk(context: Context, apkFile: File) {
//        val intent = Intent(Intent.ACTION_VIEW)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//        val apkUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            FileProvider.getUriForFile(context, "${context.packageName}.updater.provider", apkFile)
//        } else {
//            Uri.fromFile(apkFile)
//        }
//        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        context.startActivity(intent)
//    }

//    fun downloadNewApk(context: Context, url: String, token: String) {
//        val request = DownloadManager.Request(Uri.parse(url))
//            .setTitle("Downloading new version")
//            .setDescription("Downloading update...")
//            .addRequestHeader("Authorization", "token $token")  // Add the authentication token
//            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "new_version.apk")
//            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//            .setAllowedOverMetered(true)
//            .setAllowedOverRoaming(false)
//
//        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        manager.enqueue(request)
//    }

//    fun checkForUpdate(currentVersion: String, onUpdateAvailable: (downloadUrl: String) -> Unit) {
//        val request = Request.Builder()
//            .url("https://api.github.com/repos/{owner}/{repo}/releases/latest")
//            .addHeader("Authorization", "token $token")  // Add the authentication header
//            .build()
//
//        client.newCall(request).execute().use { response ->
//            if (response.isSuccessful) {
//                val json = JSONObject(response.body?.string() ?: "")
//                val latestVersion = json.getString("tag_name")
//                val downloadUrl = json.getJSONArray("assets")
//                    .getJSONObject(0)
//                    .getString("browser_download_url")
//
//                if (currentVersion != latestVersion) {
//                    onUpdateAvailable(downloadUrl)
//                }
//            }
//        }
//    }
}

/*
{
    "tag_name": "v1.1",
    "assets": [
    {
        "browser_download_url": "https://github.com/{owner}/{repo}/releases/download/v1.1/app-release.apk"
    }
    ]
}
*/