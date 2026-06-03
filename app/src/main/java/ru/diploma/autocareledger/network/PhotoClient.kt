package ru.diploma.autocareledger.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object PhotoClient {

    fun uploadPhoto(context: Context, token: String, uri: Uri): String? {
        val boundary = "Boundary-${UUID.randomUUID()}"
        val connection = URL("${BackendConfig.BASE_URL}/cars/photos").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.doInput = true
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        val filename = getFileName(context, uri) ?: "photo.jpg"

        connection.outputStream.use { outputStream ->
            val writer = OutputStreamWriter(outputStream, "UTF-8")

            // Add file part
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"photo\"; filename=\"$filename\"\r\n")
            writer.append("Content-Type: image/jpeg\r\n\r\n")
            writer.flush()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(outputStream)
            }
            outputStream.flush()

            writer.append("\r\n")
            writer.append("--$boundary--\r\n")
            writer.flush()
        }

        if (connection.responseCode in 200..299) {
            val responseStr = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseStr)
            return json.optString("url", null)
        } else {
            val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
            System.err.println("PhotoClient upload failed: ${connection.responseCode} - $error")
            return null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }
        return result
    }
}
