package ru.diploma.autocareledger.network

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class AuthSession(
    val token: String,
    val name: String,
    val email: String
)

object AuthClient {
    fun register(name: String, email: String, password: String): AuthSession =
        auth(
            path = "/auth/register",
            payload = JSONObject()
                .put("name", name)
                .put("email", email)
                .put("password", password)
        )

    fun login(email: String, password: String): AuthSession =
        auth(
            path = "/auth/login",
            payload = JSONObject()
                .put("email", email)
                .put("password", password)
        )

    private fun auth(path: String, payload: JSONObject): AuthSession {
        val connection = (URL("${BackendConfig.BASE_URL}$path").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 8_000
            readTimeout = 8_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(payload.toString())
        }
        val responseText = if (connection.responseCode in 200..299) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val message = connection.errorStream?.bufferedReader()?.use { it.readText() }
            throw IllegalStateException(parseErrorMessage(message) ?: "Ошибка авторизации")
        }
        val response = JSONObject(responseText)
        val user = response.getJSONObject("user")
        return AuthSession(
            token = response.getString("token"),
            name = user.optString("name"),
            email = user.optString("email")
        )
    }

    private fun parseErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return runCatching { JSONObject(raw).optString("message") }.getOrNull()?.takeIf(String::isNotBlank)
    }
}
