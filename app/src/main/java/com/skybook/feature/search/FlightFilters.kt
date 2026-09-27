package com.skybook.feature.search

import com.skybook.data.model.Flight
import java.time.LocalDateTime

enum class SortOption { CHEAPEST, FASTEST, EARLIEST }

/** Departure time buckets used by the time filter. [endHour] is exclusive. */
enum class TimeSlot(val startHour: Int, val endHour: Int) {
    EARLY_MORNING(0, 6),
    MORNING(6, 12),
    AFTERNOON(12, 18),
    EVENING(18, 24),
}

enum class StopsFilter { ANY, NON_STOP, ONE_STOP }

/** Everything the user picked in the filter sheet. Empty sets / nulls mean "no filter". */
data class FlightFilter(
    val stops: StopsFilter = StopsFilter.ANY,
    val airlines: Set<String> = emptySet(),
    val timeSlots: Set<TimeSlot> = emptySet(),
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
) {
    /** Number shown on the Filters button badge. */
    val activeCount: Int
        get() = listOf(
            stops != StopsFilter.ANY,
            airlines.isNotEmpty(),
            timeSlots.isNotEmpty(),
            minPrice != null || maxPrice != null,
        ).count { it }
}

/** Pure functions (no Android code) so they're easy to unit test. */
object FlightFilters {

    fun apply(flights: List<Flight>, filter: FlightFilter, sort: SortOption): List<Flight> {
        val filtered = flights.filter { flight ->
            val stopsOk = when (filter.stops) {
                StopsFilter.ANY -> true
                StopsFilter.NON_STOP -> flight.stops == 0
                StopsFilter.ONE_STOP -> flight.stops == 1
            }
            val airlineOk = filter.airlines.isEmpty() || flight.airlineName in filter.airlines
            val hour = LocalDateTime.parse(flight.departureTime).hour
            val timeOk = filter.timeSlots.isEmpty() ||
                filter.timeSlots.any { hour >= it.startHour && hour < it.endHour }
            val priceOk = (filter.minPrice == null || flight.price >= filter.minPrice) &&
                (filter.maxPrice == null || flight.price <= filter.maxPrice)
            stopsOk && airlineOk && timeOk && priceOk
        }
        return when (sort) {
            SortOption.CHEAPEST -> filtered.sortedWith(compareBy({ it.price }, { it.departureTime }))
            SortOption.FASTEST -> filtered.sortedWith(compareBy({ it.durationMinutes }, { it.price }))
            SortOption.EARLIEST -> filtered.sortedWith(compareBy({ it.departureTime }, { it.price }))
        }
    }
}
