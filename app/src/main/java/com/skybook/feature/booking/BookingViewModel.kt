package com.skybook.feature.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.core.util.PnrGenerator
import com.skybook.data.model.BaggageOption
import com.skybook.data.model.Booking
import com.skybook.data.model.BookingStatus
import com.skybook.data.model.FareOption
import com.skybook.data.model.FareType
import com.skybook.data.model.Flight
import com.skybook.data.model.MealOption
import com.skybook.data.model.Passenger
import com.skybook.data.model.SeatMap
import com.skybook.data.repository.BookingRepository
import com.skybook.data.repository.FlightRepository
import com.skybook.data.repository.ProfileRepository
import com.skybook.feature.booking.BookingRules.isValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BookingUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val flight: Flight? = null,
    val fare: FareOption? = null,
    val passengerCount: Int = 1,
    val passengers: List<PassengerForm> = emptyList(),
    val seatMap: SeatMap? = null,
    /** Seat numbers in the order they were picked; seat i goes to passenger i. */
    val selectedSeats: List<String> = emptyList(),
    val baggage: BaggageOption = BaggageOption.NONE,
    val payment: PaymentForm = PaymentForm(),
    val isPaying: Boolean = false,
    val confirmedBooking: Booking? = null,
) {
    /** Recalculated on every change, so the total on screen is always live (FR-15). */
    val price: PriceBreakdown
        get() = BookingRules.price(
            farePerPassenger = fare?.pricePerPassenger ?: 0.0,
            passengers = passengerCount,
            seatExtras = selectedSeats.map { seatMap?.seats?.get(it)?.extraPrice ?: 0.0 },
            baggage = baggage,
            meals = passengers.map { it.meal },
        )
}

/**
 * One ViewModel shared by every step of the booking flow, so data typed on one step
 * is still there after going back (FR-14) or rotating the phone (NFR-07).
 */
@HiltViewModel
class BookingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val flightRepository: FlightRepository,
    private val bookingRepository: BookingRepository,
    profileRepository: ProfileRepository,
) : ViewModel() {

    private val flightId: String = checkNotNull(savedStateHandle["flightId"])
    private val fareType = FareType.valueOf(checkNotNull(savedStateHandle["fare"]))
    private val passengerCount: Int = checkNotNull(savedStateHandle.get<String>("pax")).toInt()

    private val _uiState = MutableStateFlow(BookingUiState(passengerCount = passengerCount))
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    init {
        // Pre-fill the first passenger with the saved profile, if there is one.
        val profile = profileRepository.profile.value
        _uiState.update {
            it.copy(passengers = List(passengerCount) { index ->
                if (index == 0) PassengerForm(fullName = profile.name, email = profile.email, phone = profile.phone)
                else PassengerForm()
            })
        }
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, loadFailed = false) }
        viewModelScope.launch {
            val flight = flightRepository.getFlight(flightId)
            _uiState.update {
                if (flight == null) it.copy(isLoading = false, loadFailed = true)
                else it.copy(
                    isLoading = false,
                    flight = flight,
                    fare = flightRepository.fareOptions(flight).first { f -> f.type == fareType },
                    seatMap = flightRepository.seatMap(flight),
                )
            }
        }
    }

    // ---- Step 1: passengers ----

    fun updatePassenger(index: Int, transform: (PassengerForm) -> PassengerForm) {
        _uiState.update { state ->
            state.copy(passengers = state.passengers.mapIndexed { i, p ->
                // Clear the errors while the user edits; they come back on the next "Continue".
                if (i == index) transform(p).copy(
                    nameError = null, ageError = null, genderError = null, emailError = null, phoneError = null
                ) else p
            })
        }
    }

    /** Checks every form. Returns true when all are valid so the screen can move on. */
    fun validatePassengers(): Boolean {
        val validated = _uiState.value.passengers.mapIndexed { i, p -> BookingRules.validate(p, isPrimary = i == 0) }
        _uiState.update { it.copy(passengers = validated) }
        return validated.none { it.hasErrors }
    }

    // ---- Step 2: seats ----

    fun toggleSeat(number: String) {
        _uiState.update { state ->
            val seat = state.seatMap?.seats?.get(number)
            if (seat == null || !seat.isAvailable) return@update state
            val selected = state.selectedSeats
            val newSelection = when {
                number in selected -> selected - number
                selected.size < state.passengerCount -> selected + number
                // All passengers already have a seat: swap out the oldest pick.
                else -> selected.drop(1) + number
            }
            state.copy(selectedSeats = newSelection)
        }
    }

    fun skipSeats() = _uiState.update { it.copy(selectedSeats = emptyList()) }

    // ---- Step 3: add-ons ----

    fun setBaggage(option: BaggageOption) = _uiState.update { it.copy(baggage = option) }

    fun setMeal(passengerIndex: Int, meal: MealOption) = updatePassenger(passengerIndex) { it.copy(meal = meal) }

    // ---- Step 5: payment ----

    fun updatePayment(transform: (PaymentForm) -> PaymentForm) {
        _uiState.update {
            it.copy(
                payment = transform(it.payment).copy(
                    cardNumberError = null, cardNameError = null, expiryError = null, cvvError = null, upiError = null
                )
            )
        }
    }

    /** Mock payment: waits a moment, then always succeeds and saves the booking to Room. */
    fun pay() {
        val state = _uiState.value
        if (state.isPaying || state.confirmedBooking != null) return
        val flight = state.flight ?: return
        val validated = BookingRules.validate(state.payment)
        _uiState.update { it.copy(payment = validated) }
        if (!validated.isValid()) return

        _uiState.update { it.copy(isPaying = true) }
        viewModelScope.launch {
            delay(2_000) // pretend to talk to the bank
            val price = state.price
            val booking = Booking(
                bookingId = UUID.randomUUID().toString(),
                pnr = PnrGenerator.generate(),
                flight = flight,
                passengers = state.passengers.mapIndexed { i, form ->
                    Passenger(
                        fullName = form.fullName.trim(),
                        age = form.age.toInt(),
                        gender = form.gender?.name.orEmpty(),
                        email = form.email.trim().ifBlank { null },
                        phone = form.phone.trim().ifBlank { null },
                        seatNumber = state.selectedSeats.getOrNull(i),
                        meal = form.meal,
                    )
                },
                fareType = fareType,
                baggage = state.baggage,
                baseFare = price.baseFare,
                seatsTotal = price.seatsTotal,
                addOnsTotal = price.addOnsTotal,
                taxes = price.taxes,
                totalPrice = price.total,
                status = BookingStatus.CONFIRMED,
                bookedAt = System.currentTimeMillis(),
            )
            bookingRepository.save(booking)
            _uiState.update { it.copy(isPaying = false, confirmedBooking = booking) }
        }
    }
}
