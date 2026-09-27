package com.skybook.feature.flightdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AirlineSeatReclineNormal
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.AirlineLogo
import com.skybook.core.designsystem.components.ErrorState
import com.skybook.core.designsystem.components.FlightTimeline
import com.skybook.core.designsystem.components.PriceText
import com.skybook.core.designsystem.components.PriceBar
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.designsystem.theme.extended
import com.skybook.core.util.Formatters
import com.skybook.core.util.UiState
import com.skybook.data.model.FareOption
import com.skybook.data.model.FareType
import com.skybook.data.model.Flight
import com.skybook.feature.home.cabinLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailsScreen(
    onBack: () -> Unit,
    onBookNow: (flightId: String, fare: FareType, passengers: Int) -> Unit,
    viewModel: FlightDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.flight_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        },
        bottomBar = {
            val content = state.content
            if (content is UiState.Success) {
                PriceBar(
                    total = state.total,
                    passengers = state.passengers,
                    buttonText = stringResource(R.string.book_now),
                    onClick = { onBookNow(content.data.flight.id, state.selectedFare, state.passengers) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val content = state.content) {
                UiState.Loading, UiState.Empty ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                is UiState.Error -> ErrorState(onRetry = viewModel::load, modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> DetailsContent(
                    flight = content.data.flight,
                    fares = content.data.fares,
                    selectedFare = state.selectedFare,
                    onSelectFare = viewModel::selectFare
                )
            }
        }
    }
}

@Composable
private fun DetailsContent(
    flight: Flight,
    fares: List<FareOption>,
    selectedFare: FareType,
    onSelectFare: (FareType) -> Unit,
) {
    val selected = fares.first { it.type == selectedFare }
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AirlineLogo(flight.airlineLogoUrl, flight.airlineCode, flight.airlineName, size = 44.dp)
                Spacer(Modifier.width(Spacing.md))
                Column {
                    Text(flight.airlineName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${flight.flightNumber} · ${cabinLabel(flight.cabinClass)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(Formatters.longDate(flight.departureTime), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(Spacing.sm))
            FlightTimeline(flight)
            Spacer(Modifier.height(Spacing.sm))
            Row {
                Text(
                    flight.from.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(Spacing.lg))
                Text(
                    flight.to.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        SectionCard(title = stringResource(R.string.flight_info)) {
            InfoRow(Icons.Outlined.Flight, stringResource(R.string.aircraft), flight.aircraft)
            InfoRow(Icons.Outlined.Schedule, stringResource(R.string.duration), Formatters.duration(flight.durationMinutes))
            InfoRow(
                Icons.Outlined.Luggage,
                stringResource(R.string.baggage),
                stringResource(R.string.baggage_value, selected.baggageKg)
            )
            InfoRow(
                Icons.Outlined.AirlineSeatReclineNormal,
                stringResource(R.string.seats_left),
                pluralStringResource(R.plurals.seats_left_count, flight.seatsAvailable, flight.seatsAvailable)
            )
        }

        Text(stringResource(R.string.choose_fare), style = MaterialTheme.typography.titleMedium)
        fares.forEach { fare ->
            FareOptionCard(
                fare = fare,
                cheapest = fares.minOf { it.pricePerPassenger },
                selected = fare.type == selectedFare,
                onClick = { onSelectFare(fare.type) }
            )
        }

        SectionCard(title = stringResource(R.string.fare_rules)) {
            RuleRow(
                ok = selected.isRefundable,
                text = if (selected.isRefundable) stringResource(R.string.rule_refundable)
                else stringResource(R.string.rule_non_refundable)
            )
            RuleRow(
                ok = selected.freeDateChange,
                text = if (selected.freeDateChange) stringResource(R.string.rule_free_date_change)
                else stringResource(R.string.rule_paid_date_change)
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(Spacing.md))
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RuleRow(ok: Boolean, text: String) {
    Row(Modifier.padding(vertical = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
            contentDescription = null,
            tint = if (ok) MaterialTheme.extended.success else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.width(Spacing.md))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FareOptionCard(fare: FareOption, cheapest: Double, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(Spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(fareName(fare.type), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(
                        R.string.fare_summary,
                        fare.baggageKg,
                        if (fare.isRefundable) stringResource(R.string.refundable) else stringResource(R.string.non_refundable)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                PriceText(fare.pricePerPassenger, style = MaterialTheme.typography.titleMedium)
                val diff = fare.pricePerPassenger - cheapest
                if (diff > 0) {
                    Text(
                        stringResource(R.string.price_difference, Formatters.price(diff)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun fareName(type: FareType): String = when (type) {
    FareType.SAVER -> stringResource(R.string.fare_saver)
    FareType.FLEXI -> stringResource(R.string.fare_flexi)
}
