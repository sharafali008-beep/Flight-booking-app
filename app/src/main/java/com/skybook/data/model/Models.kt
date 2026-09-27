package com.skybook.data.model

import kotlinx.serialization.Serializable

// @Serializable lets us store these objects as JSON inside the Room database.

@Serializable
data class Airport(
    val code: String,        // "BLR"
    val city: String,        // "Bengaluru"
    val name: String         // "Kempegowda International Airport"
)

@Serializable
data class Flight(
    val id: String,
    val airlineName: String,
    val airlineCode: String,      // "6E"
    val airlineLogoUrl: String,
    val flightNumber: String,     // "6E 2134"
    val from: Airport,
    val to: Airport,
    val departureTime: String,    // ISO date-time, e.g. "2026-09-28T06:10"
    val arrivalTime: String,
    val durationMinutes: Int,
    val stops: Int,
    val price: Double,            // Saver fare for one passenger
    val cabinClass: CabinClass,
    val seatsAvailable: Int,
    val isRefundable: Boolean,
    val baggageKg: Int,
    val aircraft: String
)

@Serializable
enum class CabinClass { ECONOMY, BUSINESS }

@Serializable
data class Passenger(
    val fullName: String,
    val age: Int,
    val gender: String,
    val email: String?,
    val phone: String?,
    val seatNumber: String? = null,
    val meal: MealOption = MealOption.NONE
)

data class Seat(
    val number: String,        // "12A"
    val isAvailable: Boolean,
    val isPremium: Boolean,
    val extraPrice: Double
)

/**
 * Seat layout of an aircraft cabin.
 * [columns] holds seat letters left to right; a null entry is the aisle.
 */
data class SeatMap(
    val rows: List<Int>,
    val columns: List<Char?>,
    val seats: Map<String, Seat>
)

@Serializable
enum class FareType { SAVER, FLEXI }

/** A fare option shown on the Flight Details screen. */
data class FareOption(
    val type: FareType,
    val pricePerPassenger: Double,
    val isRefundable: Boolean,
    val baggageKg: Int,
    val freeDateChange: Boolean
)

@Serializable
enum class MealOption(val price: Double) { NONE(0.0), VEG(350.0), NON_VEG(400.0) }

@Serializable
enum class BaggageOption(val extraKg: Int, val price: Double) {
    NONE(0, 0.0), KG_5(5, 1_500.0), KG_10(10, 2_800.0), KG_15(15, 4_000.0)
}

data class Booking(
    val bookingId: String,
    val pnr: String,
    val flight: Flight,
    val passengers: List<Passenger>,
    val fareType: FareType,
    val baggage: BaggageOption,
    val baseFare: Double,
    val seatsTotal: Double,
    val addOnsTotal: Double,
    val taxes: Double,
    val totalPrice: Double,
    val status: BookingStatus,
    val bookedAt: Long
)

enum class BookingStatus { CONFIRMED, CANCELLED, COMPLETED }
