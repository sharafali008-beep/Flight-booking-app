package com.skybook.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.core.util.UiState
import com.skybook.data.model.Airport
import com.skybook.data.model.CabinClass
import com.skybook.data.model.Flight
import com.skybook.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** What the user has entered in the search panel. */
data class SearchForm(
    val from: Airport,
    val to: Airport,
    val date: LocalDate,
    val passengers: Int = 1,
    val cabin: CabinClass = CabinClass.ECONOMY,
) {
    val isValid: Boolean get() = from.code != to.code
}

data class HomeUiState(
    val form: SearchForm,
    val flights: UiState<List<Flight>> = UiState.Loading,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FlightRepository,
) : ViewModel() {

    val airports: List<Airport> = repository.airports

    private val _uiState = MutableStateFlow(
        HomeUiState(
            form = SearchForm(
                from = airports.first { it.code == "BLR" },
                to = airports.first { it.code == "DEL" },
                date = LocalDate.now(),
            )
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadFlights()
    }

    /** Loads all flights for the chosen date and class (the list under the search panel). */
    fun loadFlights(isRefresh: Boolean = false) {
        loadJob?.cancel()
        val form = _uiState.value.form
        _uiState.update {
            if (isRefresh) it.copy(isRefreshing = true) else it.copy(flights = UiState.Loading)
        }
        loadJob = viewModelScope.launch {
            val result = try {
                var flights = repository.getFlightsOn(form.date, form.cabin)
                // Late at night every flight today has left, so show tomorrow's instead.
                if (flights.isEmpty() && form.date == LocalDate.now()) {
                    val tomorrow = form.date.plusDays(1)
                    flights = repository.getFlightsOn(tomorrow, form.cabin)
                    if (flights.isNotEmpty()) updateForm { it.copy(date = tomorrow) }
                }
                if (flights.isEmpty()) UiState.Empty else UiState.Success(flights)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                UiState.Error(e.message)
            }
            _uiState.update { it.copy(flights = result, isRefreshing = false) }
        }
    }

    fun refresh() = loadFlights(isRefresh = true)

    fun setFrom(airport: Airport) = updateForm { it.copy(from = airport) }

    fun setTo(airport: Airport) = updateForm { it.copy(to = airport) }

    fun swapAirports() = updateForm { it.copy(from = it.to, to = it.from) }

    fun setDate(date: LocalDate) {
        updateForm { it.copy(date = date) }
        loadFlights()
    }

    fun setCabin(cabin: CabinClass) {
        if (cabin == _uiState.value.form.cabin) return
        updateForm { it.copy(cabin = cabin) }
        loadFlights()
    }

    fun changePassengers(delta: Int) = updateForm {
        it.copy(passengers = (it.passengers + delta).coerceIn(MIN_PASSENGERS, MAX_PASSENGERS))
    }

    private fun updateForm(transform: (SearchForm) -> SearchForm) {
        _uiState.update { it.copy(form = transform(it.form)) }
    }

    companion object {
        const val MIN_PASSENGERS = 1
        const val MAX_PASSENGERS = 9
    }
}
