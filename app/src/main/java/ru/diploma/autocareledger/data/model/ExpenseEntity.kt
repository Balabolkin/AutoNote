package ru.diploma.autocareledger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
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
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long,
    val category: String,
    val amount: Double,
    val fuelLiters: Double? = null,
    val mileage: Int,
    val dateMillis: Long,
    val title: String,
    val notes: String,
    val workCost: Double? = null,
    val partsCost: Double? = null,
    val shopName: String? = null,
    val partName: String? = null,
    val partNumber: String? = null,
    val partBrand: String? = null,
    val assembly: String? = null
)
