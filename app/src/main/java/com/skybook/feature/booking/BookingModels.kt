package com.skybook.feature.booking

import androidx.annotation.StringRes
import com.skybook.R
import com.skybook.data.model.BaggageOption
import com.skybook.data.model.MealOption
import kotlin.math.roundToInt

enum class Gender { MALE, FEMALE, OTHER }

/** What the user is typing into one passenger form. Errors are string resource ids (null = OK). */
data class PassengerForm(
    val fullName: String = "",
    val age: String = "",
    val gender: Gender? = null,
    val email: String = "",
    val phone: String = "",
    val meal: MealOption = MealOption.NONE,
    @StringRes val nameError: Int? = null,
    @StringRes val ageError: Int? = null,
    @StringRes val genderError: Int? = null,
    @StringRes val emailError: Int? = null,
    @StringRes val phoneError: Int? = null,
) {
    val hasErrors: Boolean
        get() = listOf(nameError, ageError, genderError, emailError, phoneError).any { it != null }
}

enum class PaymentMethod { CARD, UPI }

data class PaymentForm(
    val method: PaymentMethod = PaymentMethod.CARD,
    val cardNumber: String = "",
    val cardName: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val upiId: String = "",
    @StringRes val cardNumberError: Int? = null,
    @StringRes val cardNameError: Int? = null,
    @StringRes val expiryError: Int? = null,
    @StringRes val cvvError: Int? = null,
    @StringRes val upiError: Int? = null,
)

data class PriceBreakdown(
    val farePerPassenger: Double,
    val passengers: Int,
    val baseFare: Double,
    val seatsTotal: Double,
    val addOnsTotal: Double,
    val taxes: Double,
) {
    val total: Double get() = baseFare + seatsTotal + addOnsTotal + taxes
}

/** Price and validation rules. Kept free of Android code so they can be unit tested. */
object BookingRules {
    /** Taxes & fees are 12% of the base fare. */
    const val TAX_RATE = 0.12

    fun price(
        farePerPassenger: Double,
        passengers: Int,
        seatExtras: List<Double>,
        baggage: BaggageOption,
        meals: List<MealOption>,
    ): PriceBreakdown {
        val baseFare = farePerPassenger * passengers
        return PriceBreakdown(
            farePerPassenger = farePerPassenger,
            passengers = passengers,
            baseFare = baseFare,
            seatsTotal = seatExtras.sum(),
            addOnsTotal = baggage.price + meals.sumOf { it.price },
            taxes = (baseFare * TAX_RATE).roundToInt().toDouble(),
        )
    }

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRegex = Regex("^[6-9][0-9]{9}$")        // Indian mobile number
    private val nameRegex = Regex("^[A-Za-z][A-Za-z .'-]{1,59}$")
    private val upiRegex = Regex("^[A-Za-z0-9._-]{2,}@[A-Za-z]{2,}$")

    /**
     * Returns the form with error fields filled in.
     * Contact details (email + phone) are required for the first passenger only.
     */
    fun validate(form: PassengerForm, isPrimary: Boolean): PassengerForm {
        val age = form.age.toIntOrNull()
        return form.copy(
            nameError = when {
                form.fullName.isBlank() -> R.string.error_name_required
                !nameRegex.matches(form.fullName.trim()) -> R.string.error_name_invalid
                else -> null
            },
            ageError = when {
                form.age.isBlank() -> R.string.error_age_required
                age == null || age !in 0..120 -> R.string.error_age_invalid
                else -> null
            },
            genderError = if (form.gender == null) R.string.error_gender_required else null,
            emailError = when {
                form.email.isBlank() -> if (isPrimary) R.string.error_email_required else null
                !emailRegex.matches(form.email.trim()) -> R.string.error_email_invalid
                else -> null
            },
            phoneError = when {
                form.phone.isBlank() -> if (isPrimary) R.string.error_phone_required else null
                !phoneRegex.matches(form.phone.trim()) -> R.string.error_phone_invalid
                else -> null
            },
        )
    }

    fun validate(form: PaymentForm): PaymentForm = when (form.method) {
        PaymentMethod.CARD -> form.copy(
            cardNumberError = if (form.cardNumber.filter { it.isDigit() }.length == 16) null else R.string.error_card_number,
            cardNameError = if (form.cardName.isBlank()) R.string.error_card_name else null,
            expiryError = if (Regex("^(0[1-9]|1[0-2])/[0-9]{2}$").matches(form.expiry)) null else R.string.error_expiry,
            cvvError = if (Regex("^[0-9]{3}$").matches(form.cvv)) null else R.string.error_cvv,
            upiError = null,
        )
        PaymentMethod.UPI -> form.copy(
            upiError = if (upiRegex.matches(form.upiId.trim())) null else R.string.error_upi,
            cardNumberError = null, cardNameError = null, expiryError = null, cvvError = null,
        )
    }

    fun PaymentForm.isValid() = listOf(cardNumberError, cardNameError, expiryError, cvvError, upiError).all { it == null }
}
