package com.specure.updater.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import timber.log.Timber
import java.io.File

class GithubAppUpdater(
    private val downloadClient: HttpClient,
    private val jsonClient: HttpClient,
    private val appContext: Context,
) : Updater {

    private val _progressState = MutableSharedFlow<UpdatingStatus>(replay = 1)
    override val updateStatus: SharedFlow<UpdatingStatus> = _progressState

    private var updateFile: File? = null
    private var versionName: String? = null
    private var downloadUrl: String? = null

    override suspend fun checkAndInstall() {
        checkForUpdate()
        updateStatus.first { status ->
            if (status is UpdatingStatus.NewVersionFound) {
                Timber.d("New version found and installing")
                CoroutineScope(Dispatchers.IO).launch {
                    downloadAndUpdateSilently()
                }
                Timber.d("Collected state: $status")
                true
            } else {
                false
            }
        }
    }

    override suspend fun checkForUpdate() {
        clearState()
        _progressState.emit(UpdatingStatus.Checking)
        val latestRelease = fetchLatestRelease()
        Timber.d("Latest release found: $latestRelease")
        latestRelease?.let { release ->
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
            val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            apkAsset?.let { apk ->
                val repoVersion = release.tag_name
                val latestVersion = getVersionToInstall(repoVersion, packageInfo.versionName)
                Timber.d("Latest release version: ${release.tag_name} vs. installed: ${packageInfo.versionName} - install: $latestVersion")
                if (repoVersion == latestVersion) {
                    versionName = latestVersion
                    downloadUrl = apk.url
                    Timber.d("Latest release version to update to: ${release.tag_name}")
                    updateFile = File(appContext.getExternalFilesDir(null), apk.name)
                    _progressState.emit(UpdatingStatus.NewVersionFound(latestVersion))
                    return
                }
            }
        }
        _progressState.emit(UpdatingStatus.NoNewVersion)
    }

    override suspend fun downloadAndInstallUpdate() {
        if (downloadUrl.isNullOrEmpty() || updateFile == null) {
            _progressState.emit(UpdatingStatus.ErrorDownloading("No file"))
        }
        downloadUrl?.let { fileDownloadUrl ->
            updateFile?.let { file ->
                downloadApk(fileDownloadUrl, file)
                installApk(appContext, file)
                return
            }
        }
        _progressState.emit(UpdatingStatus.ErrorDownloading("Error during downloading"))
    }

    private suspend fun fetchLatestRelease(): Release? {
        return try {
            jsonClient.get("${BuildConfig.GITHUB_API_REPO_URL}/releases/latest").body<Release>()
        } catch (e: Exception) {
            yield()
            e.printStackTrace()
            _progressState.emit(UpdatingStatus.ErrorCheckingUpdate(e.message))
            null
        }
    }

    private suspend fun downloadAndUpdateSilently() {
        if (downloadUrl.isNullOrEmpty() || updateFile == null) {
            _progressState.emit(UpdatingStatus.ErrorDownloading("No file"))
        }
        downloadUrl?.let { fileDownloadUrl ->
            updateFile?.let { file ->
                downloadApk(fileDownloadUrl, file)
                installUpdateSilently()
            }
        }
    }

    /**
     * This still needs user interaction as system asks for user to confirm the installation process
     */
    private suspend fun installUpdateSilently() {
        _progressState.emit(UpdatingStatus.InstallingSilently)
        try {
            val sessionParams =
                PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val packageInstaller = appContext.packageManager.packageInstaller
            updateFile?.let { file ->
                val sessionId = packageInstaller.createSession(sessionParams)
                val session = packageInstaller.openSession(sessionId)
                val apkUri = FileProvider.getUriForFile(
                    appContext,
                    "${appContext.packageName}.updater.provider",
                    file
                )
                appContext.contentResolver.openInputStream(apkUri).use { apkInput ->
                    requireNotNull(apkInput) { "$apkUri: InputStream was null" }
                    val sessionStream = session.openWrite("temp.apk", 0, -1)
                    sessionStream.buffered().use { bufferedStream ->
                        apkInput.copyTo(bufferedStream)
                        bufferedStream.flush()
                        session.fsync(sessionStream)
                    }
                }
                val receiverIntent = Intent(appContext, GithubAppUpdateReloader::class.java)
                val flags =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    } else {
                        PendingIntent.FLAG_UPDATE_CURRENT
                    }

                val receiverPendingIntent =
                    PendingIntent.getBroadcast(appContext, 0, receiverIntent, flags)
                session.commit(receiverPendingIntent.intentSender)
                session.close()
            }
        } catch (e: Exception) {
            yield()
            e.printStackTrace()
        }
    }

    private fun clearState() {
        updateFile = null
        versionName = null
        downloadUrl = null
    }

    suspend fun downloadApk(downloadUrl: String, outputFile: File) {
        _progressState.emit(UpdatingStatus.Downloading)
        try {
            val response = downloadClient.get(downloadUrl) {
                headers.remove(HttpHeaders.ContentType)
                headers.remove(HttpHeaders.Accept)
                header(HttpHeaders.Accept, ContentType.Application.OctetStream)
                header(HttpHeaders.ContentType, ContentType.Application.OctetStream)
            }

            outputFile.outputStream().use { fileStream ->
                currentCoroutineContext().ensureActive()
                val byteChannel: ByteReadChannel = response.bodyAsChannel()
                val buffer = ByteArray(1024)
                while (!byteChannel.isClosedForRead) {
                    val bytesRead = byteChannel.readAvailable(buffer, 0, buffer.size)
                    currentCoroutineContext().ensureActive()
                    if (bytesRead > 0) {
                        fileStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        } catch (e: Exception) {
            yield()
            _progressState.emit(UpdatingStatus.ErrorDownloading(e.message))
            e.printStackTrace()
        }

    }

    private suspend fun installApk(context: Context, apkFile: File) {
        _progressState.emit(UpdatingStatus.InstallingInteractive)
        val apkUri: Uri =
            FileProvider.getUriForFile(context, "${context.packageName}.updater.provider", apkFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        _progressState.emit(UpdatingStatus.Idle)
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

}