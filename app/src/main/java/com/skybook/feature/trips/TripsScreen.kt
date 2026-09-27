package com.skybook.feature.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.MessageState
import com.skybook.core.designsystem.components.PriceText
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.designsystem.theme.extended
import com.skybook.core.util.Formatters
import com.skybook.core.util.UiState
import com.skybook.data.model.Booking
import com.skybook.data.model.BookingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(
    onBookingClick: (bookingId: String) -> Unit,
    onBookFlight: () -> Unit,
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_trips)) }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                UiState.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                UiState.Empty, is UiState.Error -> MessageState(
                    icon = Icons.Outlined.Luggage,
                    title = stringResource(R.string.no_trips_title),
                    message = stringResource(R.string.no_trips_message),
                    actionLabel = stringResource(R.string.search_flights),
                    onAction = onBookFlight,
                    modifier = Modifier.fillMaxSize()
                )
                is UiState.Success -> {
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = {
                            Text(stringResource(R.string.upcoming_count, s.data.upcoming.size))
                        })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = {
                            Text(stringResource(R.string.past_count, s.data.past.size))
                        })
                    }
                    val list = if (tab == 0) s.data.upcoming else s.data.past
                    if (list.isEmpty()) {
                        MessageState(
                            icon = Icons.Outlined.Luggage,
                            title = stringResource(
                                if (tab == 0) R.string.no_upcoming_trips else R.string.no_past_trips
                            ),
                            message = "",
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(list, key = { it.bookingId }) { booking ->
                                TripCard(booking, onClick = { onBookingClick(booking.bookingId) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripCard(booking: Booking, onClick: () -> Unit) {
    val flight = booking.flight
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.route_title, flight.from.code, flight.to.code),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(booking.displayStatus())
            }
            Text(
                "${flight.from.city} → ${flight.to.city}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${Formatters.shortDate(flight.departureTime)} · ${Formatters.time(flight.departureTime)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.pnr_value, booking.pnr),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                PriceText(booking.totalPrice, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val (label, color) = when (status) {
        BookingStatus.CONFIRMED -> stringResource(R.string.status_confirmed) to MaterialTheme.extended.success
        BookingStatus.CANCELLED -> stringResource(R.string.status_cancelled) to MaterialTheme.colorScheme.error
        BookingStatus.COMPLETED -> stringResource(R.string.status_completed) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}
