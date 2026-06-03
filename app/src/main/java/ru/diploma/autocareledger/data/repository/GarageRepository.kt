package ru.diploma.autocareledger.data.repository

import android.content.SharedPreferences
import ru.diploma.autocareledger.data.local.GarageDao
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.data.model.ReminderEntity
import ru.diploma.autocareledger.network.GarageSnapshot
import ru.diploma.autocareledger.network.SyncProfile
import ru.diploma.autocareledger.network.SyncClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RestoreResult(
    val success: Boolean,
    val hasGarageData: Boolean
)

class GarageRepository(
    private val dao: GarageDao,
    private val accountPrefs: SharedPreferences,
    private val appSettings: SharedPreferences
) {
    fun observeCars() = dao.observeCars()

    fun observeArchivedCars() = dao.observeArchivedCars()

    fun observeExpenses(carId: Long) = dao.observeExpenses(carId)

    fun observeReminders(carId: Long) = dao.observeReminders(carId)

    fun observeAllReminders() = dao.observeAllReminders()

    suspend fun addCar(car: CarEntity) = dao.insertCar(car)

    suspend fun updateCar(car: CarEntity) = dao.updateCar(car)

    suspend fun deleteCar(carId: Long) = dao.deleteCar(carId)

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)

    suspend fun updateExpense(expense: ExpenseEntity) = dao.updateExpense(expense)

    suspend fun deleteExpense(expenseId: Long) = dao.deleteExpense(expenseId)

    suspend fun deleteReminder(reminderId: Long) = dao.deleteReminder(reminderId)

    suspend fun addReminder(reminder: ReminderEntity) = dao.insertReminder(reminder)

    suspend fun updateReminder(reminder: ReminderEntity) = dao.updateReminder(reminder)

    suspend fun setReminderCompleted(reminder: ReminderEntity, completed: Boolean) {
        dao.updateReminder(reminder.copy(isCompleted = completed))
    }

    suspend fun syncToServer(context: android.content.Context, token: String): Boolean {
        // Upload any local photos first
        val cars = dao.getAllCars()
        for (car in cars) {
            if (car.photoUri?.startsWith("content://") == true || car.photoUri?.startsWith("file://") == true) {
                val remoteUrl = ru.diploma.autocareledger.network.PhotoClient.uploadPhoto(context, token, android.net.Uri.parse(car.photoUri))
                if (remoteUrl != null) {
                    dao.updateCar(car.copy(photoUri = remoteUrl))
                }
            }
        }

        val snapshot = GarageSnapshot(
            profile = currentProfile(),
            cars = dao.getAllCars(), // Refetch to get updated URLs
            expenses = dao.getAllExpenses(),
            reminders = dao.getAllReminders()
        )
        return withContext(Dispatchers.IO) {
            runCatching { SyncClient.uploadSnapshot(token, snapshot) }.getOrDefault(false)
        }
    }

    suspend fun restoreFromServer(token: String): RestoreResult {
        val snapshot = withContext(Dispatchers.IO) {
            runCatching { SyncClient.downloadSnapshot(token) }.getOrNull()
        }
            ?: return RestoreResult(success = false, hasGarageData = false)
        saveProfile(snapshot.profile)
        val hasGarageData = snapshot.cars.isNotEmpty() || snapshot.expenses.isNotEmpty() || snapshot.reminders.isNotEmpty()
        if (!hasGarageData) {
            return RestoreResult(success = true, hasGarageData = false)
        }
        dao.deleteAllReminders()
        dao.deleteAllExpenses()
        dao.deleteAllCars()
        dao.insertCars(snapshot.cars)
        dao.insertExpenses(snapshot.expenses)
        dao.insertReminders(snapshot.reminders)
        return RestoreResult(success = true, hasGarageData = true)
    }

    private fun currentProfile(): SyncProfile =
        SyncProfile(
            name = accountPrefs.getString("name", "").orEmpty(),
            phone = accountPrefs.getString("phone", "").orEmpty(),
            email = accountPrefs.getString("email", "").orEmpty(),
            themePreference = appSettings.getString("theme", "System").orEmpty().ifBlank { "System" },
            preferredFuelType = appSettings.getString("preferred_fuel_type", "АИ-95").orEmpty().ifBlank { "АИ-95" },
            preferredCurrency = appSettings.getString("preferred_currency", "RUB").orEmpty().ifBlank { "RUB" }
        )

    private fun saveProfile(profile: SyncProfile) {
        accountPrefs.edit()
            .putString("name", profile.name)
            .putString("phone", profile.phone)
            .putString("email", profile.email)
            .apply()
        appSettings.edit()
            .putString("theme", profile.themePreference)
            .putString("preferred_fuel_type", profile.preferredFuelType)
            .putString("preferred_currency", profile.preferredCurrency)
            .apply()
    }

    suspend fun clearLocalData() {
        dao.deleteAllReminders()
        dao.deleteAllExpenses()
        dao.deleteAllCars()
        accountPrefs.edit().clear().apply()
    }

    suspend fun toggleCarSharing(token: String, carId: Long, shared: Boolean): String? =
        withContext(Dispatchers.IO) {
            SyncClient.toggleCarSharing(token, carId, shared)
        }
}
