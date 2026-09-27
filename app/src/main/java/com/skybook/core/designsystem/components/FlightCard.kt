package com.skybook.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skybook.R
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.data.mock.MockFlightData
import com.skybook.data.model.Flight

/**
 * Flight card layout from the design guide:
 * ```
 * [Logo] IndiGo · 6E 2134                  ₹4,599
 * 06:10 ──────── 2h 45m ──────── 08:55
 * BLR            Non-stop            DEL
 * ```
 */
@Composable
fun FlightCard(
    flight: Flight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AirlineLogo(flight.airlineLogoUrl, flight.airlineCode, flight.airlineName)
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(flight.airlineName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        flight.flightNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PriceText(flight.price)
            }
            Spacer(Modifier.height(Spacing.lg))
            FlightTimeline(flight)
        }
    }
}

/** The "06:10 ──── 2h 45m ──── 08:55" row with city codes and stops underneath. */
@Composable
fun FlightTimeline(flight: Flight, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(Formatters.time(flight.departureTime), style = MaterialTheme.typography.titleLarge)
            Text(
                flight.from.code,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(
            Modifier.weight(1f).padding(horizontal = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                Formatters.duration(flight.durationMinutes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Icon(
                    Icons.Filled.Flight,
                    contentDescription = null,
                    modifier = Modifier.rotate(90f),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                stopsLabel(flight.stops),
                style = MaterialTheme.typography.labelMedium,
                color = if (flight.stops == 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(Formatters.time(flight.arrivalTime), style = MaterialTheme.typography.titleLarge)
            Text(
                flight.to.code,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun stopsLabel(stops: Int): String =
    if (stops == 0) stringResource(R.string.non_stop)
    else pluralStringResource(R.plurals.stops_count, stops, stops)

@Preview
@Composable
private fun FlightCardPreview() {
    SkyBookTheme {
        Box(Modifier.padding(Spacing.lg)) {
            FlightCard(flight = MockFlightData.previewFlight, onClick = {})
        }
    }
}

@Preview
@Composable
private fun FlightCardDarkPreview() {
    SkyBookTheme(darkTheme = true) {
        Box(Modifier.padding(Spacing.lg), contentAlignment = Alignment.Center) {
            FlightCard(flight = MockFlightData.previewFlight, onClick = {})
        }
    }
}

