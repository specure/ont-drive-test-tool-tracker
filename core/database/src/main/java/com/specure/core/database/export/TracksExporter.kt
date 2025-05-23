package com.specure.core.database.export

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.opencsv.bean.StatefulBeanToCsv
import com.opencsv.bean.StatefulBeanToCsvBuilder
import com.specure.core.database.R
import com.specure.core.database.entity.TrackEntity
import com.specure.core.database.mappers.toTrackEntity
import com.specure.core.domain.track.LocalTrackDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class TracksExporter(
    private val context: Context,
    private val notificationManager: NotificationManager,
    private val trackDataSource: LocalTrackDataSource,
    private val databaseWorkerScheduler: DatabaseWorkerScheduler
) {

    // TODO: split to notification handler, file saver, track exporter should just provide tracks to export and call others
    private val _exportStateFlow: MutableStateFlow<ExportState> =
        MutableStateFlow(ExportState.Initial)
    val exportStateFlow: StateFlow<ExportState> = _exportStateFlow
    private val notificationId = 12358 // Unique ID for the notification
    private val channelId = "export_channel"
    private val channelName = "Export Channel"
    private var latestUpdateTime = System.currentTimeMillis()

    init {
        createNotificationChannel()
    }


    sealed class ExportState {
        data object Initial : ExportState()
        data class Exporting(val progress: Int) : ExportState()
        data class Success(val file: File) : ExportState()
        data object NothingToExport : ExportState()
        data object Error : ExportState()
    }

    fun initializeState() {
        _exportStateFlow.value = ExportState.Exporting(1)
    }

    suspend fun exportFile(
        format: String? = "csv",
        fileName: String? = null
    ) {
        withContext(Dispatchers.IO) {
            var writer: Writer? = null
            try {
                _exportStateFlow.value = ExportState.Exporting(1)
                updateNotificationProgress(1)
                val name = if (fileName != null) {
                    "$fileName.$format"
                } else {
                    "track_export_${System.currentTimeMillis().formatMillisToDateString()}.$format"
                }
                val outputFile = if (Build.VERSION_CODES.Q >= Build.VERSION.SDK_INT) {
                    File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        name
                    )
                } else {
                    File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                        name
                    )
                }
                val trackList = trackDataSource.getTracksForExport().first()
                if (trackList.isEmpty()) {
                    _exportStateFlow.value = ExportState.NothingToExport
                    showNothingToExportNotification()
//                        return@collect
                } else {
                    writer = FileWriter(outputFile)
                    val beanToCsv: StatefulBeanToCsv<TrackEntity> =
                        StatefulBeanToCsvBuilder<TrackEntity>(writer).build()

                    val totalSize = trackList.size
                    Timber.d("Exporting $totalSize items")

                    trackList.forEachIndexed { index, track ->
                        beanToCsv.write(track.toTrackEntity())
                        val progress =
                            ((index * 100) / totalSize).coerceAtLeast(1).coerceAtMost(100)
                        _exportStateFlow.value = ExportState.Exporting(progress)
                        updateProgressNotificationCorrectly(progress)
                    }
                    _exportStateFlow.value = ExportState.Success(outputFile)
                    showExportCompleteNotification(outputFile)
                    databaseWorkerScheduler.scheduleWork(WorkType.MarkAsExported)
                }
//                }
            } catch (e: Exception) {
                e.printStackTrace()
                _exportStateFlow.value = ExportState.Error
                notificationManager.cancel(notificationId)
                if (e is CancellationException) {
                    throw e
                }
            } finally {
                writer?.close()
            }
        }
    }

    private fun updateProgressNotificationCorrectly(progressPercentage: Int) {
        val currentTime = System.currentTimeMillis()
        val isTimeToUpdate =
            currentTime >= latestUpdateTime + NOTIFICATION_PROGRESS_UPDATE_STEP_MILLIS
        val isFinished = progressPercentage == 100
        if (isFinished || isTimeToUpdate) {
            updateNotificationProgress(progressPercentage)
            latestUpdateTime = currentTime
        }
    }

    fun openFile(file: File, onError: (e: Exception) -> Unit) {
        val parsedUri = convertUriForUseInIntent(file.path)
        parsedUri?.let { fileUri ->
            val intent = if (Build.VERSION_CODES.Q >= Build.VERSION.SDK_INT) {
                createOpenFileIntentV29(fileUri)
            } else {
                createOpenFileIntent(fileUri)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    private fun createOpenFileIntent(fileUri: Uri): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fileUri, getMimeType(fileUri))
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.action = Intent.ACTION_VIEW
        return intent
    }

    private fun createOpenFileIntentV29(fileUri: Uri): Intent {
        val filename = fileUri.path?.getFileNameWithExtFromUriOrDefault() ?: "file"
        val file = File(
            ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS)[0],
            filename
        )
        val uri = Uri.fromFile(file)
        val uriParsed = convertUriForUseInIntent(uri.toString())
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uriParsed, getMimeType(fileUri))
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        return intent
    }

    private fun convertUriForUseInIntent(localUri: String?): Uri? {
        if (localUri != null) {
            val parsedUri = Uri.parse(localUri)
            parsedUri.path?.let { parsedPath ->
                return FileProvider.getUriForFile(
                    context, context.packageName + ".provider",
                    File(parsedPath)
                )
            }
        }
        return null
    }

    private fun getMimeType(uri: Uri): String? {
        val isContent = uri.scheme == "content"
        return if (isContent) {
            val cr = context.contentResolver
            cr.getType(uri)
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotificationProgress(progress: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.export_complete))
            .setContentText(context.getString(R.string.export_progress, progress))
            .setSmallIcon(R.drawable.arrow_down)
            .setProgress(100, progress, false)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showNothingToExportNotification() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(context.getString(R.string.error))
            .setContentText(context.getString(R.string.no_tracks_to_export))
            .setSmallIcon(R.drawable.arrow_down)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun showExportCompleteNotification(file: File) {
        val parsedUri = convertUriForUseInIntent(file.path)
        parsedUri?.let { fileUri ->
            // Open the downloaded file
            val intent = if (Build.VERSION_CODES.Q >= Build.VERSION.SDK_INT) {
                createOpenFileIntentV29(fileUri)
            } else {
                createOpenFileIntent(fileUri)
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val filename = file.path.getFileNameWithExtFromUriOrDefault()

            val notification = NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getString(R.string.export_complete))
                .setContentText(context.getString(R.string.export_complete_description, filename))
                .setSmallIcon(R.drawable.arrow_down)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(notificationId, notification)
        }
    }

    private fun String.getFileNameWithExtFromUriOrDefault(): String {
        var url = this
        if (!TextUtils.isEmpty(url)) {
            url = removeFragmentPart(url)
            url = removeQueryPart(url)

            val filenamePos: Int = url.lastIndexOf('/')
            val filename: String = if (0 <= filenamePos) url.substring(filenamePos + 1) else url

            val notContainsSpecialCharacters = Pattern.matches("[a-zA-Z_0-9.\\-()%]+", filename)
            if (filename.isNotEmpty() &&
                notContainsSpecialCharacters
            ) {
                return filename
            }
        }
        return "Default"
    }

    private fun removeFragmentPart(uri: String): String {
        var url = uri
        val fragment: Int = url.lastIndexOf('#')
        if (fragment > 0) {
            url = url.substring(0, fragment)
        }
        return url
    }

    private fun removeQueryPart(uri: String): String {
        var url = uri
        val query: Int = url.lastIndexOf('?')
        if (query > 0) {
            url = url.substring(0, query)
        }
        return url
    }

    companion object {
        private const val NOTIFICATION_PROGRESS_UPDATE_STEP_MILLIS = 1000
    }

    /**
     * android files cannot contain ":"
     */
    fun Long.formatMillisToDateString(): String {
        val instant = Instant.ofEpochMilli(this)
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
        return dateTime.format(formatter)
    }
}