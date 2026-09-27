package com.skybook.core.util

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Helpers that turn raw values into text for the UI. */
object Formatters {
    private val indiaLocale = Locale("en", "IN")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
    private val shortDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH)
    private val longDateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)

    /** 4599.0 -> "₹4,599" (Indian digit grouping, no paise). */
    fun price(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(indiaLocale)
        format.maximumFractionDigits = 0
        format.minimumFractionDigits = 0
        return format.format(amount)
    }

    /** 165 -> "2h 45m" */
    fun duration(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "${m}m"
            m == 0 -> "${h}h"
            else -> "${h}h ${m}m"
        }
    }

    fun time(isoDateTime: String): String = LocalDateTime.parse(isoDateTime).format(timeFormat)

    fun shortDate(isoDateTime: String): String = LocalDateTime.parse(isoDateTime).format(shortDateFormat)

    fun shortDate(date: LocalDate): String = date.format(shortDateFormat)

    fun longDate(isoDateTime: String): String = LocalDateTime.parse(isoDateTime).format(longDateFormat)

    fun dateTimeFromMillis(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
            .format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.ENGLISH))
}
