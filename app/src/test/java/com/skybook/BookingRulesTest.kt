package com.skybook

import com.skybook.core.util.Formatters
import com.skybook.core.util.PnrGenerator
import com.skybook.data.model.BaggageOption
import com.skybook.data.model.MealOption
import com.skybook.feature.booking.BookingRules
import com.skybook.feature.booking.Gender
import com.skybook.feature.booking.PassengerForm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingRulesTest {

    @Test
    fun `total is base fare times passengers plus seats plus add-ons plus taxes`() {
        val price = BookingRules.price(
            farePerPassenger = 4_599.0,
            passengers = 2,
            seatExtras = listOf(450.0, 0.0),
            baggage = BaggageOption.KG_5,
            meals = listOf(MealOption.VEG, MealOption.NONE),
        )
        assertEquals(9_198.0, price.baseFare, 0.0)
        assertEquals(450.0, price.seatsTotal, 0.0)
        assertEquals(1_850.0, price.addOnsTotal, 0.0)   // 1500 baggage + 350 meal
        assertEquals(1_104.0, price.taxes, 0.0)         // 12% of 9198, rounded
        assertEquals(9_198.0 + 450 + 1_850 + 1_104, price.total, 0.0)
    }

    private val valid = PassengerForm(
        fullName = "Asha Rao", age = "29", gender = Gender.FEMALE,
        email = "asha@example.com", phone = "9876543210"
    )

    @Test
    fun `valid primary passenger has no errors`() {
        assertFalse(BookingRules.validate(valid, isPrimary = true).hasErrors)
    }

    @Test
    fun `empty name and bad email and phone are rejected`() {
        val result = BookingRules.validate(valid.copy(fullName = "", email = "nope", phone = "12345"), true)
        assertNotNull(result.nameError)
        assertNotNull(result.emailError)
        assertNotNull(result.phoneError)
    }

    @Test
    fun `contact details optional for other passengers`() {
        val result = BookingRules.validate(valid.copy(email = "", phone = ""), isPrimary = false)
        assertNull(result.emailError)
        assertNull(result.phoneError)
        assertTrue(BookingRules.validate(valid.copy(email = ""), isPrimary = true).hasErrors)
    }

    @Test
    fun `pnr looks like SKY plus five characters`() {
        repeat(50) { assertTrue(PnrGenerator.generate().matches(Regex("^SKY[A-Z2-9]{5}$"))) }
    }

    @Test
    fun `formats prices and durations`() {
        assertEquals("₹4,599", Formatters.price(4599.0))
        // Lakh grouping (₹1,00,000) comes from Android's ICU and isn't available in plain JVM tests.
        assertEquals("2h 45m", Formatters.duration(165))
        assertEquals("1h", Formatters.duration(60))
    }
}
