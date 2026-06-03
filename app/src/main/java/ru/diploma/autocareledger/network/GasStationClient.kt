package ru.diploma.autocareledger.network

import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class GasStationPriceReport(
    val stationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val fuelType: String,
    val price: Double
)

data class GasStationPriceItem(
    val id: Long,
    val stationName: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val fuelType: String,
    val price: Double,
    val reportedAt: Long
)

object GasStationClient {
    fun reportPrice(token: String, report: GasStationPriceReport): Boolean {
        return try {
            val connection = openConnection("/gas-stations/prices", token, "POST")
            val body = JSONObject().apply {
                put("stationName", report.stationName)
                put("address", report.address)
                put("latitude", report.latitude)
                put("longitude", report.longitude)
                put("fuelType", report.fuelType)
                put("price", report.price)
            }
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
            }
            val code = connection.responseCode
            code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun fetchPrices(token: String): List<GasStationPriceItem> {
        return try {
            val connection = openConnection("/gas-stations/prices", token, "GET")
            if (connection.responseCode !in 200..299) {
                return emptyList()
            }
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            val array = JSONArray(raw)
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                GasStationPriceItem(
                    id = item.getLong("id"),
                    stationName = item.getString("stationName"),
                    address = item.getString("address"),
                    latitude = item.getDouble("latitude"),
                    longitude = item.getDouble("longitude"),
                    fuelType = item.getString("fuelType"),
                    price = item.getDouble("price"),
                    reportedAt = item.getLong("reportedAt")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun openConnection(path: String, token: String, method: String): HttpURLConnection {
        return (URL("${BackendConfig.BASE_URL}$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 8_000
            readTimeout = 8_000
            doOutput = method != "GET"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
    }
}
