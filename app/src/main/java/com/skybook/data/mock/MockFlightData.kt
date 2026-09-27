package com.skybook.data.mock

import com.skybook.data.model.Airport
import com.skybook.data.model.CabinClass
import com.skybook.data.model.Flight
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Fake flight data for Phase 1.
 * A fixed timetable ([templates]) is repeated for every day in the booking window,
 * with prices that vary a little per day so results look realistic.
 */
object MockFlightData {

    /** How many days ahead (including today) flights can be searched. */
    const val BOOKING_WINDOW_DAYS = 30

    val airports: List<Airport> = listOf(
        Airport("BLR", "Bengaluru", "Kempegowda International Airport"),
        Airport("DEL", "New Delhi", "Indira Gandhi International Airport"),
        Airport("BOM", "Mumbai", "Chhatrapati Shivaji Maharaj International Airport"),
        Airport("MAA", "Chennai", "Chennai International Airport"),
        Airport("HYD", "Hyderabad", "Rajiv Gandhi International Airport"),
        Airport("CCU", "Kolkata", "Netaji Subhas Chandra Bose International Airport"),
        Airport("GOI", "Goa", "Dabolim Airport"),
    )

    private fun airport(code: String) = airports.first { it.code == code }

    private data class Airline(val name: String, val code: String, val hasBusiness: Boolean)

    private val indigo = Airline("IndiGo", "6E", hasBusiness = false)
    private val airIndia = Airline("Air India", "AI", hasBusiness = true)
    private val akasa = Airline("Akasa Air", "QP", hasBusiness = false)
    private val spiceJet = Airline("SpiceJet", "SG", hasBusiness = false)
    private val aiExpress = Airline("Air India Express", "IX", hasBusiness = false)

    private data class Template(
        val airline: Airline,
        val number: Int,
        val from: String,
        val to: String,
        val departure: String,   // "HH:mm"
        val durationMinutes: Int,
        val stops: Int,
        val basePrice: Int,
        val aircraft: String,
    )

    private val templates = listOf(
        Template(indigo, 2134, "BLR", "DEL", "06:10", 165, 0, 4599, "Airbus A320neo"),
        Template(airIndia, 503, "BLR", "DEL", "09:30", 170, 0, 5299, "Airbus A321neo"),
        Template(akasa, 1411, "BLR", "DEL", "14:45", 175, 0, 4399, "Boeing 737 MAX 8"),
        Template(spiceJet, 8171, "BLR", "DEL", "18:20", 285, 1, 3899, "Boeing 737-800"),
        Template(indigo, 2135, "DEL", "BLR", "07:00", 170, 0, 4799, "Airbus A320neo"),
        Template(airIndia, 504, "DEL", "BLR", "20:15", 175, 0, 5499, "Airbus A321neo"),
        Template(indigo, 5302, "BLR", "BOM", "06:45", 105, 0, 3299, "Airbus A320neo"),
        Template(akasa, 1102, "BLR", "BOM", "12:10", 110, 0, 3099, "Boeing 737 MAX 8"),
        Template(airIndia, 640, "BOM", "BLR", "17:40", 110, 0, 3899, "Airbus A320neo"),
        Template(indigo, 5303, "BOM", "BLR", "21:05", 105, 0, 3399, "Airbus A320neo"),
        Template(airIndia, 865, "DEL", "BOM", "08:00", 130, 0, 4999, "Boeing 787-8"),
        Template(indigo, 2175, "DEL", "BOM", "13:30", 135, 0, 4299, "Airbus A321neo"),
        Template(spiceJet, 8701, "BOM", "DEL", "10:15", 140, 0, 3999, "Boeing 737 MAX 8"),
        Template(airIndia, 866, "BOM", "DEL", "19:00", 130, 0, 5199, "Boeing 787-8"),
        Template(indigo, 6118, "MAA", "DEL", "05:50", 170, 0, 5099, "Airbus A321neo"),
        Template(airIndia, 440, "MAA", "BOM", "11:20", 115, 0, 3799, "Airbus A320neo"),
        Template(aiExpress, 1285, "HYD", "BLR", "07:35", 75, 0, 2599, "Boeing 737-8"),
        Template(indigo, 6417, "HYD", "DEL", "16:05", 135, 0, 4699, "Airbus A320neo"),
        Template(airIndia, 763, "CCU", "DEL", "09:05", 140, 0, 4899, "Airbus A320neo"),
        Template(indigo, 5089, "BOM", "CCU", "15:25", 165, 0, 5199, "Airbus A321neo"),
        Template(spiceJet, 1022, "BLR", "CCU", "08:40", 290, 1, 4599, "Boeing 737-800"),
        Template(indigo, 6541, "GOI", "BOM", "13:00", 70, 0, 2399, "ATR 72-600"),
        Template(akasa, 1345, "BLR", "GOI", "10:50", 75, 0, 2799, "Boeing 737 MAX 8"),
        Template(airIndia, 887, "DEL", "GOI", "06:20", 160, 0, 5699, "Airbus A320neo"),
        Template(aiExpress, 2931, "BLR", "MAA", "19:45", 60, 0, 2199, "Boeing 737-8"),
        Template(indigo, 7422, "HYD", "BOM", "22:10", 90, 0, 2999, "Airbus A320neo"),
    )

    private fun logoUrl(code: String) = "https://pics.avs.io/200/200/$code.png"

    /** Builds every flight from [startDate] for [days] days. Same input always gives the same output. */
    fun generate(startDate: LocalDate, days: Int = BOOKING_WINDOW_DAYS): List<Flight> {
        val flights = mutableListOf<Flight>()
        for (dayOffset in 0 until days) {
            val date = startDate.plusDays(dayOffset.toLong())
            for (t in templates) {
                // Seeded random = stable prices for a given flight + date.
                val random = Random(t.number * 31L + date.toEpochDay())
                val demand = 0.85 + random.nextDouble() * 0.5 + (if (dayOffset < 3) 0.15 else 0.0)
                val economyPrice = roundPrice(t.basePrice * demand)
                val cabins = if (t.airline.hasBusiness) CabinClass.entries else listOf(CabinClass.ECONOMY)
                for (cabin in cabins) {
                    val departure = date.atTime(LocalTime.parse(t.departure))
                    val arrival = departure.plusMinutes(t.durationMinutes.toLong())
                    val isBusiness = cabin == CabinClass.BUSINESS
                    flights += Flight(
                        id = "${t.airline.code}${t.number}-${cabin.name.first()}-$date",
                        airlineName = t.airline.name,
                        airlineCode = t.airline.code,
                        airlineLogoUrl = logoUrl(t.airline.code),
                        flightNumber = "${t.airline.code} ${t.number}",
                        from = airport(t.from),
                        to = airport(t.to),
                        departureTime = departure.toString(),
                        arrivalTime = arrival.toString(),
                        durationMinutes = t.durationMinutes,
                        stops = t.stops,
                        price = if (isBusiness) roundPrice(economyPrice * 3.2) else economyPrice,
                        cabinClass = cabin,
                        seatsAvailable = if (isBusiness) random.nextInt(2, 12) else random.nextInt(3, 60),
                        isRefundable = isBusiness,
                        baggageKg = if (isBusiness) 35 else 15,
                        aircraft = t.aircraft,
                    )
                }
            }
        }
        return flights
    }

    /** Rounds to a "retail" price ending in 99, e.g. 4612.3 -> 4599. */
    private fun roundPrice(value: Double): Double = ((value / 100).roundToInt() * 100 - 1).toDouble()

    /** A fixed flight used by @Preview functions. */
    val previewFlight = Flight(
        id = "6E2134-E-2026-10-01",
        airlineName = "IndiGo",
        airlineCode = "6E",
        airlineLogoUrl = "",
        flightNumber = "6E 2134",
        from = Airport("BLR", "Bengaluru", "Kempegowda International Airport"),
        to = Airport("DEL", "New Delhi", "Indira Gandhi International Airport"),
        departureTime = "2026-10-01T06:10",
        arrivalTime = "2026-10-01T08:55",
        durationMinutes = 165,
        stops = 0,
        price = 4599.0,
        cabinClass = CabinClass.ECONOMY,
        seatsAvailable = 12,
        isRefundable = false,
        baggageKg = 15,
        aircraft = "Airbus A320neo",
    )
}
