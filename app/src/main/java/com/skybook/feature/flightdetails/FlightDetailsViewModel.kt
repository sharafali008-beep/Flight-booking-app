package com.skybook.feature.flightdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.core.util.UiState
import com.skybook.data.model.FareOption
import com.skybook.data.model.FareType
import com.skybook.data.model.Flight
import com.skybook.data.repository.FlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlightDetailsContent(
    val flight: Flight,
    val fares: List<FareOption>,
)

data class FlightDetailsUiState(
    val content: UiState<FlightDetailsContent> = UiState.Loading,
    val passengers: Int,
    val selectedFare: FareType = FareType.SAVER,
) {
    val selectedOption: FareOption?
        get() = (content as? UiState.Success)?.data?.fares?.firstOrNull { it.type == selectedFare }

    val total: Double get() = (selectedOption?.pricePerPassenger ?: 0.0) * passengers
}

@HiltViewModel
class FlightDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FlightRepository,
) : ViewModel() {

    private val flightId: String = checkNotNull(savedStateHandle["flightId"])

    private val _uiState = MutableStateFlow(
        FlightDetailsUiState(passengers = checkNotNull(savedStateHandle.get<String>("pax")).toInt())
    )
    val uiState: StateFlow<FlightDetailsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(content = UiState.Loading) }
        viewModelScope.launch {
            val flight = repository.getFlight(flightId)
            _uiState.update {
                it.copy(
                    content = if (flight == null) UiState.Error()
                    else UiState.Success(FlightDetailsContent(flight, repository.fareOptions(flight)))
                )
            }
        }
    }

    fun selectFare(type: FareType) = _uiState.update { it.copy(selectedFare = type) }
}
