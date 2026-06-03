package ru.diploma.autocareledger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.diploma.autocareledger.data.model.CarEntity
import ru.diploma.autocareledger.data.model.ExpenseEntity
import ru.diploma.autocareledger.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GarageDao {
    @Query("SELECT * FROM cars WHERE isArchived = 0 ORDER BY id DESC")
    fun observeCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM cars ORDER BY id DESC")
    suspend fun getAllCars(): List<CarEntity>

    @Query("SELECT * FROM cars WHERE isArchived = 1 ORDER BY id DESC")
    fun observeArchivedCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM expenses WHERE carId = :carId ORDER BY dateMillis DESC, id DESC")
    fun observeExpenses(carId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC, id DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM reminders WHERE carId = :carId ORDER BY isCompleted ASC, dueDateMillis ASC, dueMileage ASC")
    fun observeReminders(carId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDateMillis ASC, dueMileage ASC")
    fun observeAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDateMillis ASC, dueMileage ASC")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: CarEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCars(cars: List<CarEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: Long)

    @Update
    suspend fun updateCar(car: CarEntity)

    @Query("DELETE FROM cars WHERE id = :carId")
    suspend fun deleteCar(carId: Long)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: Long)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM cars")
    suspend fun deleteAllCars()
}
