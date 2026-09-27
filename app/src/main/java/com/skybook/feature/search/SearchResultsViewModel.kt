package com.skybook.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.core.util.UiState
import com.skybook.data.model.Airport
import com.skybook.data.model.CabinClass
import com.skybook.data.model.Flight
import com.skybook.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SearchResultsUiState(
    val from: Airport,
    val to: Airport,
    val date: LocalDate,
    val passengers: Int,
    val cabin: CabinClass,
    /** Every flight on the route, before filters. */
    val allFlights: UiState<List<Flight>> = UiState.Loading,
    val sort: SortOption = SortOption.CHEAPEST,
    val filter: FlightFilter = FlightFilter(),
) {
    /** Flights after filters and sorting; this is what the list shows. */
    val visibleFlights: List<Flight>
        get() = (allFlights as? UiState.Success)?.data?.let { FlightFilters.apply(it, filter, sort) }.orEmpty()

    val airlines: List<String>
        get() = (allFlights as? UiState.Success)?.data?.map { it.airlineName }?.distinct()?.sorted().orEmpty()

    val priceBounds: ClosedFloatingPointRange<Double>?
        get() = (allFlights as? UiState.Success)?.data?.takeIf { it.isNotEmpty() }
            ?.let { list -> list.minOf { it.price }..list.maxOf { it.price } }
}

@HiltViewModel
class SearchResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FlightRepository,
) : ViewModel() {

    // Values passed in the navigation route (see navigation/Routes.kt).
    private val fromCode: String = checkNotNull(savedStateHandle["from"])
    private val toCode: String = checkNotNull(savedStateHandle["to"])

    private val _uiState = MutableStateFlow(
        SearchResultsUiState(
            from = repository.airports.first { it.code == fromCode },
            to = repository.airports.first { it.code == toCode },
            date = LocalDate.parse(checkNotNull(savedStateHandle.get<String>("date"))),
            passengers = checkNotNull(savedStateHandle.get<String>("pax")).toInt(),
            cabin = CabinClass.valueOf(checkNotNull(savedStateHandle["cabin"])),
        )
    )
    val uiState: StateFlow<SearchResultsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        val s = _uiState.value
        _uiState.update { it.copy(allFlights = UiState.Loading) }
        viewModelScope.launch {
            val result = try {
                val flights = repository.searchFlights(s.from.code, s.to.code, s.date, s.cabin, s.passengers)
                if (flights.isEmpty()) UiState.Empty else UiState.Success(flights)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                UiState.Error(e.message)
            }
            _uiState.update { it.copy(allFlights = result) }
        }
    }

    fun setSort(sort: SortOption) = _uiState.update { it.copy(sort = sort) }

    fun setFilter(filter: FlightFilter) = _uiState.update { it.copy(filter = filter) }

    fun clearFilters() = _uiState.update { it.copy(filter = FlightFilter()) }
}
