package com.skybook.data.local

import androidx.room.TypeConverter
import com.skybook.data.model.Flight
import com.skybook.data.model.Passenger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Room can only store simple values, so objects are converted to/from JSON text. */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun flightToJson(flight: Flight): String = json.encodeToString(flight)

    @TypeConverter
    fun jsonToFlight(value: String): Flight = json.decodeFromString(value)

    @TypeConverter
    fun passengersToJson(passengers: List<Passenger>): String = json.encodeToString(passengers)

    @TypeConverter
    fun jsonToPassengers(value: String): List<Passenger> = json.decodeFromString(value)
}
