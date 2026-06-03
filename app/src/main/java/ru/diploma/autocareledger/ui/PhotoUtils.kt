package ru.diploma.autocareledger.ui

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun getTmpFileUri(context: Context): Uri {
    val tmpFile = File.createTempFile("tmp_image_file_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tmpFile
    )
}
