package com.skybook.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skybook.data.model.Passenger
import com.skybook.data.repository.BookingRepository
import com.skybook.data.repository.Profile
import com.skybook.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProfileForm(val name: String, val email: String, val phone: String, val saved: Boolean = false)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    bookingRepository: BookingRepository,
) : ViewModel() {

    private val _form = MutableStateFlow(
        profileRepository.profile.value.let { ProfileForm(it.name, it.email, it.phone) }
    )
    val form: StateFlow<ProfileForm> = _form.asStateFlow()

    /** Everyone the user has booked for before, without duplicates (FR-19). */
    val savedPassengers: StateFlow<List<Passenger>> = bookingRepository.observeBookings()
        .map { bookings ->
            bookings.flatMap { it.passengers }.distinctBy { it.fullName.lowercase() }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun update(transform: (ProfileForm) -> ProfileForm) = _form.update { transform(it).copy(saved = false) }

    fun save() {
        val f = _form.value
        profileRepository.save(Profile(f.name.trim(), f.email.trim(), f.phone.trim()))
        _form.update { it.copy(saved = true) }
    }
}
