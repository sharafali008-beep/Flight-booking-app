package com.skybook.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.core.util.UiState
import com.skybook.data.model.Booking
import com.skybook.data.model.BookingStatus
import com.skybook.data.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import javax.inject.Inject

data class TripsContent(val upcoming: List<Booking>, val past: List<Booking>)

/** A flight that has already departed counts as completed. */
fun Booking.displayStatus(now: LocalDateTime = LocalDateTime.now()): BookingStatus =
    if (status == BookingStatus.CONFIRMED && isPast(now)) BookingStatus.COMPLETED else status

fun Booking.isPast(now: LocalDateTime = LocalDateTime.now()): Boolean =
    LocalDateTime.parse(flight.departureTime).isBefore(now)

@HiltViewModel
class TripsViewModel @Inject constructor(
    repository: BookingRepository,
) : ViewModel() {

    // Room emits a new list whenever bookings change, so this screen updates by itself.
    val uiState: StateFlow<UiState<TripsContent>> = repository.observeBookings()
        .map { bookings ->
            if (bookings.isEmpty()) UiState.Empty
            else {
                val (past, upcoming) = bookings.partition { it.isPast() }
                UiState.Success(
                    TripsContent(
                        upcoming = upcoming.sortedBy { it.flight.departureTime },
                        past = past.sortedByDescending { it.flight.departureTime },
                    )
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)
}
