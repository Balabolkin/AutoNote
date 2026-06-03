package ru.diploma.autocareledger.network

import org.json.JSONArray
import org.json.JSONObject
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.data.model.ReminderEntity
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class GarageSnapshot(
    val updatedAt: Long = 0,
    val profile: SyncProfile = SyncProfile(),
    val cars: List<CarEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList()
)

data class SyncProfile(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val themePreference: String = "System",
    val preferredFuelType: String = "АИ-95",
    val preferredCurrency: String = "RUB"
)

object SyncClient {
    fun uploadSnapshot(token: String, snapshot: GarageSnapshot): Boolean {
        val connection = openConnection("/garage/cars?backup=1", token, "POST")
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(snapshot.toJson().toString())
        }
        return connection.responseCode in 200..299
    }

    fun downloadSnapshot(token: String): GarageSnapshot {
        val connection = openConnection("/garage/cars?backup=1", token, "GET")
        if (connection.responseCode !in 200..299) {
            throw IllegalStateException("Не удалось загрузить резервную копию")
        }
        val raw = connection.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(raw).toGarageSnapshot()
    }

    fun toggleCarSharing(token: String, carId: Long, shared: Boolean): String? {
        val connection = openConnection("/api/user/shared-cars/toggle", token, "POST")
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(JSONObject().put("carId", carId).put("shared", shared).toString())
        }
        if (connection.responseCode !in 200..299) {
            return null
        }
        val raw = connection.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(raw).optString("token").takeIf { it.isNotBlank() && it != "null" }
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

private fun GarageSnapshot.toJson(): JSONObject =
    JSONObject()
        .put("updatedAt", System.currentTimeMillis())
        .put("profile", profile.toJson())
        .put("cars", JSONArray().also { array -> cars.forEach { array.put(it.toJson()) } })
        .put("expenses", JSONArray().also { array -> expenses.forEach { array.put(it.toJson()) } })
        .put("reminders", JSONArray().also { array -> reminders.forEach { array.put(it.toJson()) } })

private fun SyncProfile.toJson(): JSONObject =
    JSONObject()
        .put("name", name)
        .put("phone", phone)
        .put("email", email)
        .put("themePreference", themePreference)
        .put("preferredFuelType", preferredFuelType)
        .put("preferredCurrency", preferredCurrency)

private fun CarEntity.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("brand", brand)
        .put("model", model)
        .put("generation", generation)
        .put("restyling", restyling)
        .put("trim", trim)
        .put("year", year)
        .put("plateNumber", plateNumber)
        .put("mileage", mileage)
        .put("tankVolumeLiters", tankVolumeLiters)
        .put("fuelType", fuelType)
        .put("colorName", colorName)
        .put("colorHex", colorHex)
        .put("photoUri", photoUri)
        .put("isArchived", isArchived)

private fun ExpenseEntity.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("carId", carId)
        .put("category", category)
        .put("amount", amount)
        .put("fuelLiters", fuelLiters)
        .put("mileage", mileage)
        .put("dateMillis", dateMillis)
        .put("title", title)
        .put("notes", notes)
        .put("workCost", workCost)
        .put("partsCost", partsCost)
        .put("shopName", shopName)
        .put("partName", partName)
        .put("partNumber", partNumber)
        .put("partBrand", partBrand)
        .put("assembly", assembly)

private fun ReminderEntity.toJson(): JSONObject =
    JSONObject()
        .put("id", id)
        .put("carId", carId)
        .put("title", title)
        .put("category", category)
        .put("dueMileage", dueMileage)
        .put("dueDateMillis", dueDateMillis)
        .put("repeatMileageInterval", repeatMileageInterval)
        .put("repeatIntervalMonths", repeatIntervalMonths)
        .put("isCompleted", isCompleted)

private fun JSONObject.toGarageSnapshot(): GarageSnapshot =
    GarageSnapshot(
        updatedAt = optLong("updatedAt", 0),
        profile = optJSONObject("profile")?.toSyncProfile() ?: SyncProfile(),
        cars = optJSONArray("cars").toCarList(),
        expenses = optJSONArray("expenses").toExpenseList(),
        reminders = optJSONArray("reminders").toReminderList()
    )

private fun JSONObject.toSyncProfile(): SyncProfile =
    SyncProfile(
        name = optString("name"),
        phone = optString("phone"),
        email = optString("email"),
        themePreference = optString("themePreference", "System"),
        preferredFuelType = optString("preferredFuelType", "АИ-95"),
        preferredCurrency = optString("preferredCurrency", "RUB")
    )

private fun JSONArray?.toCarList(): List<CarEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        CarEntity(
            id = item.optLong("id"),
            brand = item.optString("brand"),
            model = item.optString("model"),
            generation = item.optString("generation"),
            restyling = item.optString("restyling"),
            trim = item.optString("trim"),
            year = item.optInt("year"),
            plateNumber = item.optString("plateNumber"),
            mileage = item.optInt("mileage"),
            tankVolumeLiters = item.optDouble("tankVolumeLiters", 0.0),
            fuelType = item.optString("fuelType"),
            colorName = item.optString("colorName"),
            colorHex = item.optString("colorHex"),
            photoUri = item.optString("photoUri").takeIf { it.isNotBlank() && it != "null" },
            isArchived = item.optBoolean("isArchived", false)
        )
    }
}

private fun JSONArray?.toExpenseList(): List<ExpenseEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        ExpenseEntity(
            id = item.optLong("id"),
            carId = item.optLong("carId"),
            category = item.optString("category"),
            amount = item.optDouble("amount"),
            fuelLiters = if (item.isNull("fuelLiters")) null else item.optDouble("fuelLiters"),
            mileage = item.optInt("mileage"),
            dateMillis = item.optLong("dateMillis"),
            title = item.optString("title"),
            notes = item.optString("notes"),
            workCost = if (item.isNull("workCost")) null else item.optDouble("workCost"),
            partsCost = if (item.isNull("partsCost")) null else item.optDouble("partsCost"),
            shopName = if (item.isNull("shopName")) null else item.optString("shopName"),
            partName = if (item.isNull("partName")) null else item.optString("partName"),
            partNumber = if (item.isNull("partNumber")) null else item.optString("partNumber"),
            partBrand = if (item.isNull("partBrand")) null else item.optString("partBrand"),
            assembly = if (item.isNull("assembly")) null else item.optString("assembly")
        )
    }
}

private fun JSONArray?.toReminderList(): List<ReminderEntity> {
    if (this == null) return emptyList()
    return (0 until length()).map { index ->
        val item = getJSONObject(index)
        ReminderEntity(
            id = item.optLong("id"),
            carId = item.optLong("carId"),
            title = item.optString("title"),
            category = item.optString("category", "Service"),
            dueMileage = if (item.isNull("dueMileage")) null else item.optInt("dueMileage"),
            dueDateMillis = if (item.isNull("dueDateMillis")) null else item.optLong("dueDateMillis"),
            repeatMileageInterval = if (item.isNull("repeatMileageInterval")) null else item.optInt("repeatMileageInterval"),
            repeatIntervalMonths = if (item.isNull("repeatIntervalMonths")) null else item.optInt("repeatIntervalMonths"),
            isCompleted = item.optBoolean("isCompleted")
        )
    }
}
