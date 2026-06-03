package ru.diploma.autocareledger.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.diploma.autocareledger.data.repository.GarageRepository
import ru.diploma.autocareledger.ui.model.ExpenseCategory

class ReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val repository: GarageRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val cars = repository.observeCars().firstOrNull() ?: emptyList()
            val allReminders = repository.observeAllReminders().firstOrNull() ?: emptyList()

            // Filter for uncompleted reminders
            val activeReminders = allReminders.filter { !it.isCompleted }

            val now = System.currentTimeMillis()
            val threeDaysMillis = 3L * 24 * 60 * 60 * 1000

            activeReminders.forEach { reminder ->
                val car = cars.firstOrNull { it.id == reminder.carId }
                var isDue = false
                var reason = ""

                // Check date
                if (reminder.dueDateMillis != null) {
                    if (now >= reminder.dueDateMillis - threeDaysMillis) {
                        isDue = true
                        reason = if (now >= reminder.dueDateMillis) "Срок подошел!" else "Срок подходит."
                    }
                }

                // Check mileage
                if (reminder.dueMileage != null && car != null) {
                    if (car.mileage >= reminder.dueMileage - 500) {
                        isDue = true
                        reason = if (car.mileage >= reminder.dueMileage) "Пробег достигнут!" else "Скоро по пробегу."
                    }
                }

                // If due, send a push notification
                if (isDue) {
                    val categoryTitle = getCategoryTitle(reminder.category)
                    val carName = car?.displayName ?: "Автомобиль"
                    val title = "Напоминание: $carName"
                    val message = "$categoryTitle. $reason"
                    
                    NotificationHelper.createNotificationChannel(appContext)
                    NotificationHelper.showReminderNotification(
                        context = appContext,
                        id = reminder.id.toInt(),
                        title = title,
                        message = message
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun getCategoryTitle(category: String): String {
        return when (category) {
            ExpenseCategory.Maintenance.name -> "Техническое обслуживание"
            ExpenseCategory.Repair.name -> "Ремонт"
            ExpenseCategory.Parts.name -> "Запчасти"
            ExpenseCategory.Insurance.name -> "Страховка"
            ExpenseCategory.Parking.name -> "Парковка"
            ExpenseCategory.Wash.name -> "Мойка"
            ExpenseCategory.Fuel.name -> "Заправка"
            else -> "Другое"
        }
    }
}
