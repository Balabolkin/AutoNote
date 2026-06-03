package ru.diploma.autocareledger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("carId")]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long,
    val title: String,
    val category: String = "Service",
    val dueMileage: Int?,
    val dueDateMillis: Long?,
    val repeatMileageInterval: Int? = null,
    val repeatIntervalMonths: Int? = null,
    val isCompleted: Boolean = false
)
