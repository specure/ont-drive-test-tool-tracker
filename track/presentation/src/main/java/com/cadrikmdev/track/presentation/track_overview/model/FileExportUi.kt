package com.cadrikmdev.track.presentation.track_overview.model

import java.io.File

data class FileExportUi(
    val file: File? = null,
    val progress: Int? = null,
    val error: FileExportError? = null
)

sealed class FileExportError(open val message: String) {
    data class NothingToExport(override val message: String = "Nothing to export") :
        FileExportError(message)

    data class Unknown(override val message: String) : FileExportError(message)
}