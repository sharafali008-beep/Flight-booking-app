package com.skybook

import com.skybook.data.mock.MockFlightData
import com.skybook.data.model.CabinClass
import com.skybook.feature.search.FlightFilter
import com.skybook.feature.search.FlightFilters
import com.skybook.feature.search.SortOption
import com.skybook.feature.search.StopsFilter
import com.skybook.feature.search.TimeSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class FlightFiltersTest {

    private val flights = MockFlightData.generate(LocalDate.of(2026, 10, 1), days = 1)
        .filter { it.cabinClass == CabinClass.ECONOMY && it.from.code == "BLR" && it.to.code == "DEL" }

    @Test
    fun `mock data has several BLR to DEL flights`() {
        assertTrue(flights.size >= 3)
    }

    @Test
    fun `sorts cheapest, fastest and earliest`() {
        val cheapest = FlightFilters.apply(flights, FlightFilter(), SortOption.CHEAPEST)
        assertEquals(cheapest.map { it.price }.sorted(), cheapest.map { it.price })

        val fastest = FlightFilters.apply(flights, FlightFilter(), SortOption.FASTEST)
        assertEquals(fastest.map { it.durationMinutes }.sorted(), fastest.map { it.durationMinutes })

        val earliest = FlightFilters.apply(flights, FlightFilter(), SortOption.EARLIEST)
        assertEquals(earliest.map { it.departureTime }.sorted(), earliest.map { it.departureTime })
    }

    @Test
    fun `filters by stops, airline, time and price together`() {
        val nonStop = FlightFilters.apply(flights, FlightFilter(stops = StopsFilter.NON_STOP), SortOption.CHEAPEST)
        assertTrue(nonStop.all { it.stops == 0 })

        val indigo = FlightFilters.apply(flights, FlightFilter(airlines = setOf("IndiGo")), SortOption.CHEAPEST)
        assertTrue(indigo.isNotEmpty() && indigo.all { it.airlineName == "IndiGo" })

        val morning = FlightFilters.apply(flights, FlightFilter(timeSlots = setOf(TimeSlot.MORNING)), SortOption.CHEAPEST)
        assertTrue(morning.all { LocalDateTime.parse(it.departureTime).hour in 6..11 })

        val cap = flights.minOf { it.price }
        val cheap = FlightFilters.apply(flights, FlightFilter(maxPrice = cap), SortOption.CHEAPEST)
        assertTrue(cheap.all { it.price <= cap })
        assertEquals(1, FlightFilter(stops = StopsFilter.NON_STOP).activeCount)
    }
}
