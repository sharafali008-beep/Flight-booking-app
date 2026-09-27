package com.skybook.data.repository

import com.skybook.data.mock.MockFlightData
import com.skybook.data.model.Airport
import com.skybook.data.model.CabinClass
import com.skybook.data.model.FareOption
import com.skybook.data.model.FareType
import com.skybook.data.model.Flight
import com.skybook.data.model.Seat
import com.skybook.data.model.SeatMap
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

/** Where screens get flight data from. Phase 2 can swap in an API-backed implementation. */
interface FlightRepository {
    val airports: List<Airport>

    /** All flights on [date] in [cabin] that haven't departed yet, earliest first. */
    suspend fun getFlightsOn(date: LocalDate, cabin: CabinClass): List<Flight>

    /** Flights from [from] to [to] on [date] with at least [passengers] seats left. */
    suspend fun searchFlights(
        from: String,
        to: String,
        date: LocalDate,
        cabin: CabinClass,
        passengers: Int,
    ): List<Flight>

    suspend fun getFlight(id: String): Flight?

    fun fareOptions(flight: Flight): List<FareOption>

    fun seatMap(flight: Flight): SeatMap
}

@Singleton
class MockFlightRepository @Inject constructor() : FlightRepository {

    // Generated once, the first time it's needed.
    private val allFlights: List<Flight> by lazy { MockFlightData.generate(LocalDate.now()) }

    override val airports: List<Airport> = MockFlightData.airports

    /** Pretend to be a network call so loading states are visible. */
    private suspend fun fakeNetworkDelay() = delay(700)

    private fun Flight.notDeparted() = LocalDateTime.parse(departureTime).isAfter(LocalDateTime.now())

    override suspend fun getFlightsOn(date: LocalDate, cabin: CabinClass): List<Flight> {
        fakeNetworkDelay()
        return allFlights
            .filter { it.cabinClass == cabin && LocalDateTime.parse(it.departureTime).toLocalDate() == date }
            .filter { it.notDeparted() }
            .sortedBy { it.departureTime }
    }

    override suspend fun searchFlights(
        from: String,
        to: String,
        date: LocalDate,
        cabin: CabinClass,
        passengers: Int,
    ): List<Flight> = getFlightsOn(date, cabin)
        .filter { it.from.code == from && it.to.code == to && it.seatsAvailable >= passengers }

    override suspend fun getFlight(id: String): Flight? {
        delay(300)
        return allFlights.firstOrNull { it.id == id }
    }

    override fun fareOptions(flight: Flight): List<FareOption> = listOf(
        FareOption(
            type = FareType.SAVER,
            pricePerPassenger = flight.price,
            isRefundable = flight.isRefundable,
            baggageKg = flight.baggageKg,
            freeDateChange = false,
        ),
        FareOption(
            type = FareType.FLEXI,
            // Flexi costs ~15% more, rounded to the nearest ₹10.
            pricePerPassenger = flight.price + ((flight.price * 0.15) / 10).roundToInt() * 10,
            isRefundable = true,
            baggageKg = flight.baggageKg + 5,
            freeDateChange = true,
        ),
    )

    override fun seatMap(flight: Flight): SeatMap {
        val random = Random(flight.id.hashCode())
        val business = flight.cabinClass == CabinClass.BUSINESS
        val columns: List<Char?> =
            if (business) listOf('A', 'C', null, 'D', 'F') else listOf('A', 'B', 'C', null, 'D', 'E', 'F')
        val rows = if (business) (1..6).toList() else (1..30).toList()
        val seats = mutableMapOf<String, Seat>()
        for (row in rows) {
            for (letter in columns.filterNotNull()) {
                val number = "$row$letter"
                // Front rows and exit rows cost extra in economy.
                val extra = when {
                    business -> 0.0
                    row <= 3 -> 450.0
                    row == 12 || row == 13 -> 350.0
                    else -> 0.0
                }
                seats[number] = Seat(
                    number = number,
                    isAvailable = random.nextFloat() > 0.35f,
                    isPremium = extra > 0,
                    extraPrice = extra,
                )
            }
        }
        return SeatMap(rows, columns, seats)
    }
}
