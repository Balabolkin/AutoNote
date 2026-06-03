package ru.diploma.autocareledger.utils

object UnitsConverter {
    const val MILES_PER_KM = 0.621371
    const val GALLONS_PER_LITER = 0.264172

    fun kmToMiles(km: Int): Int = Math.round(km * MILES_PER_KM).toInt()
    
    fun milesToKm(miles: Int): Int = Math.round(miles / MILES_PER_KM).toInt()

    fun litersToGallons(liters: Double): Double = liters * GALLONS_PER_LITER

    fun gallonsToLiters(gallons: Double): Double = gallons / GALLONS_PER_LITER
    
    fun getDisplayMileage(rawMileage: Int, units: String): Int {
        return if (units == "imperial") kmToMiles(rawMileage) else rawMileage
    }
    
    fun getRawMileage(displayMileage: Int, units: String): Int {
        return if (units == "imperial") milesToKm(displayMileage) else displayMileage
    }
}
