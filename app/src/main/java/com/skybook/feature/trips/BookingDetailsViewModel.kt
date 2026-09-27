package com.skybook.feature.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.core.util.UiState
import com.skybook.data.model.Booking
import com.skybook.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: BookingRepository,
) : ViewModel() {

    private val bookingId: String = checkNotNull(savedStateHandle["bookingId"])

    val uiState: StateFlow<UiState<Booking>> = repository.observeBooking(bookingId)
        .map { if (it == null) UiState.Error() else UiState.Success(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    /** Mock cancellation: just marks the booking as CANCELLED (FR-18). */
    fun cancel() {
        viewModelScope.launch { repository.cancel(bookingId) }
    }
}
