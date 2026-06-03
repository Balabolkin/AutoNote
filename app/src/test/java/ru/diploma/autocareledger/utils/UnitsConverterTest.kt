package ru.diploma.autocareledger.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitsConverterTest {

    @Test
    fun `kmToMiles converts correctly`() {
        // 100 km is ~62 miles
        assertEquals(62, UnitsConverter.kmToMiles(100))
    }

    @Test
    fun `milesToKm converts correctly`() {
        // 62 miles is ~100 km
        assertEquals(100, UnitsConverter.milesToKm(62))
    }

    @Test
    fun `litersToGallons converts correctly`() {
        // 10 liters is ~2.64172 gallons
        assertEquals(2.64172, UnitsConverter.litersToGallons(10.0), 0.0001)
    }

    @Test
    fun `gallonsToLiters converts correctly`() {
        // 10 gallons is ~37.854 liters
        assertEquals(37.8541, UnitsConverter.gallonsToLiters(10.0), 0.001)
    }

    @Test
    fun `getDisplayMileage returns km for metric`() {
        assertEquals(100, UnitsConverter.getDisplayMileage(100, "metric"))
    }

    @Test
    fun `getDisplayMileage returns miles for imperial`() {
        assertEquals(62, UnitsConverter.getDisplayMileage(100, "imperial"))
    }

    @Test
    fun `getRawMileage returns km for metric`() {
        assertEquals(100, UnitsConverter.getRawMileage(100, "metric"))
    }

    @Test
    fun `getRawMileage returns km from miles for imperial`() {
        assertEquals(100, UnitsConverter.getRawMileage(62, "imperial"))
    }
}
