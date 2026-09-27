package com.skybook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.skybook.data.model.BaggageOption
import com.skybook.data.model.Booking
import com.skybook.data.model.BookingStatus
import com.skybook.data.model.FareType
import com.skybook.data.model.Flight
import com.skybook.data.model.Passenger

/** How a booking is stored in the database. Flight and passengers are saved as JSON (see [Converters]). */
@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val bookingId: String,
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
    val bookedAt: Long,
)

fun BookingEntity.toModel() = Booking(
    bookingId, pnr, flight, passengers, fareType, baggage,
    baseFare, seatsTotal, addOnsTotal, taxes, totalPrice, status, bookedAt
)

fun Booking.toEntity() = BookingEntity(
    bookingId, pnr, flight, passengers, fareType, baggage,
    baseFare, seatsTotal, addOnsTotal, taxes, totalPrice, status, bookedAt
)
