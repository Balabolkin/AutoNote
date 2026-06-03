package ru.diploma.autocareledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val model: String,
    val generation: String = "",
    val restyling: String = "",
    val trim: String = "",
    val year: Int,
    val plateNumber: String,
    val mileage: Int,
    val tankVolumeLiters: Double = 0.0,
    val fuelType: String,
    val colorName: String = "",
    val colorHex: String = "",
    val photoUri: String? = null,
    val isArchived: Boolean = false
) {
    val displayName: String
        get() = listOf(brand, model, trim).filter(String::isNotBlank).joinToString(" ")
}
