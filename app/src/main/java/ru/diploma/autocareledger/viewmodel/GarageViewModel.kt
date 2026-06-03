package ru.diploma.autocareledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.data.model.ReminderEntity
import ru.diploma.autocareledger.data.repository.GarageRepository
import ru.diploma.autocareledger.ui.model.ExpenseCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.NetworkType
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import ru.diploma.autocareledger.worker.SyncWorker

data class GarageUiState(
    val cars: List<CarEntity> = emptyList(),
    val archivedCars: List<CarEntity> = emptyList(),
    val selectedCar: CarEntity? = null,
    val expenses: List<ExpenseEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
    val allReminders: List<ReminderEntity> = emptyList(),
    val pendingReceiptQr: String? = null,
    val pendingReceiptText: String? = null,
    val restoreInProgress: Boolean = false,
    val restoreCompleted: Boolean = false,
    val restoreFailed: Boolean = false,
    val restoredGarageData: Boolean = false
) {
    val totalExpenses: Double = expenses.sumOf { it.amount }
    val totalFuelLiters: Double = expenses
        .filter { it.category == ExpenseCategory.Fuel.name }
        .sumOf { it.fuelLiters ?: 0.0 }
    val activeReminders: Int = allReminders.count { !it.isCompleted }
    val costPerKilometer: Double =
        if (selectedCar != null && selectedCar.mileage > 0) totalExpenses / selectedCar.mileage else 0.0
    val averageFuelPrice: Double =
        expenses.filter { it.category == ExpenseCategory.Fuel.name }.sumOf { it.amount }
            .let { fuelCost -> if (totalFuelLiters > 0.0) fuelCost / totalFuelLiters else 0.0 }
    val averageFuelConsumption: Double =
        calculateAverageFuelConsumption(expenses)
}

private fun calculateAverageFuelConsumption(expenses: List<ExpenseEntity>): Double {
    val fuelRecords = expenses
        .filter { it.category == ExpenseCategory.Fuel.name && (it.fuelLiters ?: 0.0) > 0.0 && it.mileage > 0 }
        .sortedBy { it.mileage }
    if (fuelRecords.size < 2) return 0.0
    val distance = (fuelRecords.last().mileage - fuelRecords.first().mileage).coerceAtLeast(0)
    if (distance == 0) return 0.0
    val litersAfterFirst = fuelRecords.drop(1).sumOf { it.fuelLiters ?: 0.0 }
    return litersAfterFirst / distance * 100.0
}

private data class RestoreState(
    val inProgress: Boolean = false,
    val completed: Boolean = false,
    val failed: Boolean = false,
    val hasGarageData: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class GarageViewModel(
    private val repository: GarageRepository,
    private val context: android.content.Context
) : ViewModel() {
    private val selectedCarId = MutableStateFlow<Long?>(null)
    private val pendingReceiptQr = MutableStateFlow<String?>(null)
    private val pendingReceiptText = MutableStateFlow<String?>(null)
    private val authToken = MutableStateFlow<String?>(null)
    private val restoreState = MutableStateFlow(RestoreState())

    private val cars: StateFlow<List<CarEntity>> = repository.observeCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val archivedCars: StateFlow<List<CarEntity>> = repository.observeArchivedCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedCar = combine(cars, selectedCarId) { currentCars, currentId ->
        currentCars.firstOrNull { it.id == currentId } ?: currentCars.firstOrNull()
    }

    private val expenses = selectedCar.flatMapLatest { car ->
        car?.let { repository.observeExpenses(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    private val reminders = selectedCar.flatMapLatest { car ->
        car?.let { repository.observeReminders(it.id) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    private val allReminders: StateFlow<List<ReminderEntity>> = repository.observeAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val selectedGarageState = combine(
        cars,
        archivedCars,
        selectedCar,
        expenses,
        reminders
    ) { currentCars, currentArchivedCars, car, currentExpenses, currentReminders ->
        GarageUiState(
            cars = currentCars,
            archivedCars = currentArchivedCars,
            selectedCar = car,
            expenses = currentExpenses,
            reminders = currentReminders
        )
    }

    private val garageWithoutReceipt = combine(
        selectedGarageState,
        allReminders
    ) { state, currentAllReminders ->
        state.copy(
            allReminders = currentAllReminders.filter { reminder ->
                state.cars.any { carEntity -> carEntity.id == reminder.carId }
            }
        )
    }

    private val garageState = combine(
        garageWithoutReceipt,
        pendingReceiptQr,
        pendingReceiptText
    ) { state, currentReceiptQr, currentReceiptText ->
        state.copy(
            pendingReceiptQr = currentReceiptQr,
            pendingReceiptText = currentReceiptText
        )
    }

    val uiState: StateFlow<GarageUiState> = combine(garageState, restoreState) { state, restore ->
        state.copy(
            restoreInProgress = restore.inProgress,
            restoreCompleted = restore.completed,
            restoreFailed = restore.failed,
            restoredGarageData = restore.hasGarageData
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GarageUiState())

    fun selectCar(carId: Long) {
        selectedCarId.value = carId
    }

    fun setAuthToken(
        token: String?,
        restoreFromServer: Boolean = false,
        syncAfterTokenSet: Boolean = true
    ) {
        authToken.value = token
        if (token.isNullOrBlank()) return
        viewModelScope.launch {
            if (restoreFromServer) {
                restoreState.value = RestoreState(inProgress = true)
                val result = repository.restoreFromServer(token)
                restoreState.value = RestoreState(
                    inProgress = false,
                    completed = true,
                    failed = !result.success,
                    hasGarageData = result.hasGarageData
                )
                return@launch
            }
            if (syncAfterTokenSet) {
                repository.syncToServer(context, token)
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncIfAuthenticated()
        }
    }

    fun addCar(
        brand: String,
        model: String,
        generation: String,
        restyling: String,
        trim: String,
        year: Int,
        plateNumber: String,
        mileage: Int,
        tankVolumeLiters: Double = 0.0,
        fuelType: String,
        colorName: String = "",
        colorHex: String = "",
        photoUri: String? = null
    ) {
        viewModelScope.launch {
            val carId = repository.addCar(
                CarEntity(
                    brand = brand.trim(),
                    model = model.trim(),
                    generation = generation.trim(),
                    restyling = restyling.trim(),
                    trim = trim.trim(),
                    year = year,
                    plateNumber = plateNumber.trim(),
                    mileage = mileage,
                    tankVolumeLiters = tankVolumeLiters,
                    fuelType = fuelType.trim(),
                    colorName = colorName.trim(),
                    colorHex = colorHex.trim(),
                    photoUri = photoUri
                )
            )
            selectedCarId.value = carId
            syncIfAuthenticated()
        }
    }

    fun updateSelectedCarMileage(mileage: Int) {
        val car = uiState.value.selectedCar ?: return
        viewModelScope.launch {
            repository.updateCar(car.copy(mileage = mileage))
            syncIfAuthenticated()
        }
    }

    fun updateSelectedCarTankVolume(tankVolumeLiters: Double) {
        val car = uiState.value.selectedCar ?: return
        viewModelScope.launch {
            repository.updateCar(car.copy(tankVolumeLiters = tankVolumeLiters.coerceAtLeast(0.0)))
            syncIfAuthenticated()
        }
    }

    fun updateCar(car: CarEntity) {
        viewModelScope.launch {
            repository.updateCar(car)
            syncIfAuthenticated()
        }
    }

    fun archiveCar(car: CarEntity, archived: Boolean) {
        viewModelScope.launch {
            repository.updateCar(car.copy(isArchived = archived))
            if (selectedCarId.value == car.id && archived) {
                selectedCarId.value = cars.value.firstOrNull { it.id != car.id }?.id
            }
            syncIfAuthenticated()
        }
    }

    fun deleteCar(car: CarEntity) {
        viewModelScope.launch {
            repository.deleteCar(car.id)
            if (selectedCarId.value == car.id) {
                selectedCarId.value = cars.value.firstOrNull { it.id != car.id }?.id
            }
            syncIfAuthenticated()
        }
    }

    fun addExpense(
        category: ExpenseCategory,
        amount: Double,
        fuelLiters: Double?,
        mileage: Int,
        title: String,
        notes: String,
        workCost: Double? = null,
        partsCost: Double? = null,
        shopName: String? = null,
        partName: String? = null,
        partNumber: String? = null,
        partBrand: String? = null,
        assembly: String? = null
    ) {
        val carId = uiState.value.selectedCar?.id ?: return
        viewModelScope.launch {
            repository.addExpense(
                ExpenseEntity(
                    carId = carId,
                    category = category.name,
                    amount = amount,
                    fuelLiters = fuelLiters?.takeIf { category == ExpenseCategory.Fuel && it > 0.0 },
                    mileage = mileage,
                    dateMillis = System.currentTimeMillis(),
                    title = title.trim(),
                    notes = notes.trim(),
                    workCost = workCost,
                    partsCost = partsCost,
                    shopName = shopName?.trim()?.takeIf { it.isNotEmpty() },
                    partName = partName?.trim()?.takeIf { it.isNotEmpty() },
                    partNumber = partNumber?.trim()?.takeIf { it.isNotEmpty() },
                    partBrand = partBrand?.trim()?.takeIf { it.isNotEmpty() },
                    assembly = assembly?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
            pendingReceiptQr.value = null
            pendingReceiptText.value = null
            syncIfAuthenticated()
        }
    }

    fun updateExpense(
        expense: ExpenseEntity,
        category: ExpenseCategory,
        amount: Double,
        fuelLiters: Double?,
        mileage: Int,
        title: String,
        notes: String,
        workCost: Double? = null,
        partsCost: Double? = null,
        shopName: String? = null,
        partName: String? = null,
        partNumber: String? = null,
        partBrand: String? = null,
        assembly: String? = null
    ) {
        viewModelScope.launch {
            repository.updateExpense(
                expense.copy(
                    category = category.name,
                    amount = amount,
                    fuelLiters = fuelLiters?.takeIf { category == ExpenseCategory.Fuel && it > 0.0 },
                    mileage = mileage,
                    title = title.trim(),
                    notes = notes.trim(),
                    workCost = workCost,
                    partsCost = partsCost,
                    shopName = shopName?.trim()?.takeIf { it.isNotEmpty() },
                    partName = partName?.trim()?.takeIf { it.isNotEmpty() },
                    partNumber = partNumber?.trim()?.takeIf { it.isNotEmpty() },
                    partBrand = partBrand?.trim()?.takeIf { it.isNotEmpty() },
                    assembly = assembly?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
            syncIfAuthenticated()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense.id)
            syncIfAuthenticated()
        }
    }

    fun setPendingReceiptQr(qr: String) {
        pendingReceiptQr.value = qr
        pendingReceiptText.value = null
    }

    fun setPendingReceiptText(text: String) {
        pendingReceiptText.value = text
        pendingReceiptQr.value = null
    }

    fun clearPendingReceiptQr() {
        pendingReceiptQr.value = null
        pendingReceiptText.value = null
    }

    fun addReminder(
        title: String,
        category: String,
        dueMileage: Int?,
        dueDateMillis: Long?,
        repeatMileageInterval: Int?,
        repeatIntervalMonths: Int?
    ) {
        val carId = uiState.value.selectedCar?.id ?: return
        viewModelScope.launch {
            repository.addReminder(
                ReminderEntity(
                    carId = carId,
                    title = title.trim(),
                    category = category,
                    dueMileage = dueMileage,
                    dueDateMillis = dueDateMillis,
                    repeatMileageInterval = repeatMileageInterval,
                    repeatIntervalMonths = repeatIntervalMonths
                )
            )
            syncIfAuthenticated()
        }
    }

    fun setReminderCompleted(reminder: ReminderEntity, completed: Boolean) {
        viewModelScope.launch {
            val selectedCar = uiState.value.selectedCar
            if (completed && (((reminder.repeatMileageInterval ?: 0) > 0) || ((reminder.repeatIntervalMonths ?: 0) > 0))) {
                val currentCarMileage = selectedCar?.mileage ?: reminder.dueMileage ?: 0
                val nextMileage = reminder.repeatMileageInterval?.let { interval ->
                    if (interval > 0) currentCarMileage + interval else null
                } ?: reminder.dueMileage
                
                val nextDate = reminder.repeatIntervalMonths?.let { months ->
                    if (months > 0) {
                        val cal = java.util.Calendar.getInstance()
                        cal.add(java.util.Calendar.MONTH, months)
                        cal.timeInMillis
                    } else null
                } ?: reminder.dueDateMillis
                
                repository.updateReminder(
                    reminder.copy(
                        dueMileage = nextMileage,
                        dueDateMillis = nextDate,
                        isCompleted = false
                    )
                )
            } else {
                repository.setReminderCompleted(reminder, completed)
            }
            syncIfAuthenticated()
        }
    }

    fun updateReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            syncIfAuthenticated()
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder.id)
            syncIfAuthenticated()
        }
    }

    private suspend fun syncIfAuthenticated() {
        if (restoreState.value.inProgress) return
        val token = authToken.value?.takeIf(String::isNotBlank) ?: return
        
        try {
            repository.syncToServer(context, token)
        } catch (e: Exception) {
            e.printStackTrace()
            // Schedule background sync via WorkManager
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
                
            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()
                
            WorkManager.getInstance(context).enqueueUniqueWork(
                "sync_worker",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            val token = authToken.value?.takeIf(String::isNotBlank)
            if (token != null) {
                try {
                    repository.syncToServer(context, token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            repository.clearLocalData()
            authToken.value = ""
            onComplete()
        }
    }

    fun shareCar(carId: Long, onShareUrlReady: (String) -> Unit, onError: (String) -> Unit) {
        val token = authToken.value?.takeIf(String::isNotBlank)
        if (token == null) {
            onError("User not authenticated")
            return
        }
        viewModelScope.launch {
            try {
                repository.syncToServer(context, token)
                val shareToken = repository.toggleCarSharing(token, carId, true)
                if (shareToken != null) {
                    val shareUrl = "${ru.diploma.autocareledger.network.BackendConfig.BASE_URL}/?shared=$shareToken"
                    onShareUrlReady(shareUrl)
                } else {
                    onError("Failed to get share link from server")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
