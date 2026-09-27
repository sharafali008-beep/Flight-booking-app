package com.skybook.feature.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.FlightTimeline
import com.skybook.core.designsystem.components.LabelValueRow
import com.skybook.core.designsystem.components.MessageState
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.core.util.UiState
import com.skybook.data.model.BaggageOption
import com.skybook.data.model.Booking
import com.skybook.data.model.BookingStatus
import com.skybook.data.model.FareType
import com.skybook.feature.booking.PriceBreakdownCard
import com.skybook.feature.booking.genderLabel
import com.skybook.feature.booking.mealLabel
import com.skybook.feature.flightdetails.fareName
import com.skybook.feature.home.cabinLabel
import androidx.compose.material.icons.outlined.ErrorOutline

/** E-ticket view of a saved booking (FR-17) with cancel option (FR-18). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    onBack: () -> Unit,
    viewModel: BookingDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmCancel by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.e_ticket)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                UiState.Loading, UiState.Empty -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> MessageState(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.booking_not_found),
                    message = "",
                    modifier = Modifier.align(Alignment.Center)
                )
                is UiState.Success -> TicketContent(s.data, onCancelClick = { confirmCancel = true })
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text(stringResource(R.string.cancel_booking_title)) },
            text = { Text(stringResource(R.string.cancel_booking_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.cancel(); confirmCancel = false }) {
                    Text(stringResource(R.string.cancel_booking), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmCancel = false }) { Text(stringResource(R.string.keep_booking)) }
            }
        )
    }
}

@Composable
private fun TicketContent(booking: Booking, onCancelClick: () -> Unit) {
    val flight = booking.flight
    val status = booking.displayStatus()
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.booking_reference),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        booking.pnr,
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusChip(status)
            }
            HorizontalDivider(Modifier.padding(vertical = Spacing.md))
            Text("${flight.airlineName} · ${flight.flightNumber}", style = MaterialTheme.typography.titleSmall)
            Text(
                "${Formatters.longDate(flight.departureTime)} · ${cabinLabel(flight.cabinClass)} · ${fareName(booking.fareType)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = Spacing.md)
            )
            FlightTimeline(flight)
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "${flight.from.name} → ${flight.to.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SectionCard(title = stringResource(R.string.travellers)) {
            booking.passengers.forEachIndexed { index, p ->
                if (index > 0) HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
                Text(p.fullName, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.age_gender, p.age.toString(), genderLabel(p.gender)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LabelValueRow(
                    stringResource(R.string.seat),
                    p.seatNumber ?: stringResource(R.string.seat_auto)
                )
                LabelValueRow(stringResource(R.string.meals), mealLabel(p.meal))
            }
        }

        SectionCard(title = stringResource(R.string.baggage)) {
            LabelValueRow(stringResource(R.string.checkin_baggage), stringResource(R.string.baggage_value, flight.baggageKg + if (booking.fareType == FareType.FLEXI) 5 else 0))
            LabelValueRow(
                stringResource(R.string.extra_baggage),
                if (booking.baggage == BaggageOption.NONE) stringResource(R.string.none)
                else stringResource(R.string.extra_kg, booking.baggage.extraKg)
            )
        }

        PriceBreakdownCard(
            farePerPassenger = booking.baseFare / booking.passengers.size,
            passengers = booking.passengers.size,
            baseFare = booking.baseFare,
            seatsTotal = booking.seatsTotal,
            addOnsTotal = booking.addOnsTotal,
            taxes = booking.taxes,
            total = booking.totalPrice,
        )

        Text(
            stringResource(R.string.booked_on, Formatters.dateTimeFromMillis(booking.bookedAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (status == BookingStatus.CONFIRMED) {
            OutlinedButton(
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(R.string.cancel_booking)) }
        }
    }
}
