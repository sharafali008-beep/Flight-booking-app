package com.skybook.navigation

import com.skybook.data.model.CabinClass
import com.skybook.data.model.FareType
import java.time.LocalDate

/**
 * All screen routes in one place. Values in {braces} are arguments that
 * ViewModels read from their SavedStateHandle.
 */
object Routes {
    const val HOME = "home"
    const val TRIPS = "trips"
    const val PROFILE = "profile"

    const val SEARCH = "search/{from}/{to}/{date}/{pax}/{cabin}"
    fun search(from: String, to: String, date: LocalDate, pax: Int, cabin: CabinClass) =
        "search/$from/$to/$date/$pax/${cabin.name}"

    const val FLIGHT_DETAILS = "flight/{flightId}/{pax}"
    fun flightDetails(flightId: String, pax: Int) = "flight/$flightId/$pax"

    // The booking steps live in a nested graph so they can share one BookingViewModel.
    const val BOOKING_GRAPH = "booking/{flightId}/{fare}/{pax}"
    fun booking(flightId: String, fare: FareType, pax: Int) = "booking/$flightId/${fare.name}/$pax"

    const val PASSENGERS = "booking/passengers"
    const val SEATS = "booking/seats"
    const val ADD_ONS = "booking/addons"
    const val REVIEW = "booking/review"
    const val PAYMENT = "booking/payment"
    const val CONFIRMATION = "booking/confirmation"

    const val BOOKING_DETAILS = "trip/{bookingId}"
    fun bookingDetails(bookingId: String) = "trip/$bookingId"
}
