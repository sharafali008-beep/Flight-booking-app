package com.skybook.feature.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.designsystem.theme.extended
import com.skybook.core.util.Formatters
import com.skybook.data.model.Seat
import com.skybook.data.model.SeatMap

/** Step 2 (optional): pick seats on the seat map. */
@Composable
fun SeatSelectionScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BookingStepScaffold(
        state = state,
        title = stringResource(R.string.step_seats),
        step = 2,
        buttonText = stringResource(R.string.continue_label),
        onBack = onBack,
        onContinue = onContinue,
        onRetry = viewModel::load,
        actions = {
            TextButton(onClick = { viewModel.skipSeats(); onContinue() }) { Text(stringResource(R.string.skip)) }
        }
    ) {
        val seatMap = state.seatMap ?: return@BookingStepScaffold
        Text(
            pluralStringResource(R.plurals.seats_selected, state.passengerCount, state.selectedSeats.size, state.passengerCount),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
        )
        SeatLegend(Modifier.padding(horizontal = Spacing.lg))
        SeatGrid(
            seatMap = seatMap,
            selected = state.selectedSeats,
            onToggle = viewModel::toggleSeat,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeatLegend(modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        LegendItem(MaterialTheme.extended.seatAvailable, stringResource(R.string.seat_available))
        LegendItem(MaterialTheme.colorScheme.primary, stringResource(R.string.seat_selected))
        LegendItem(MaterialTheme.extended.seatBooked, stringResource(R.string.seat_booked))
        LegendItem(MaterialTheme.extended.seatPremium, stringResource(R.string.seat_premium))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = Spacing.xs)) {
        Box(Modifier.size(16.dp).clip(MaterialTheme.shapes.small).background(color))
        Spacer(Modifier.width(Spacing.xs))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SeatGrid(seatMap: SeatMap, selected: List<String>, onToggle: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        // Column letters header
        item {
            Row(Modifier.fillMaxWidth()) {
                seatMap.columns.forEach { letter ->
                    Text(
                        letter?.toString() ?: "",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        items(seatMap.rows, key = { it }) { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                seatMap.columns.forEach { letter ->
                    if (letter == null) {
                        // The aisle shows the row number.
                        Text(
                            "$row",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val seat = seatMap.seats.getValue("$row$letter")
                        SeatBox(
                            seat = seat,
                            isSelected = seat.number in selected,
                            onToggle = { onToggle(seat.number) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatBox(seat: Seat, isSelected: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extended
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        !seat.isAvailable -> colors.seatBooked
        seat.isPremium -> colors.seatPremium
        else -> colors.seatAvailable
    }
    val status = when {
        isSelected -> stringResource(R.string.seat_selected)
        !seat.isAvailable -> stringResource(R.string.seat_booked)
        else -> stringResource(R.string.seat_available)
    }
    val description = if (seat.isPremium) {
        stringResource(R.string.cd_seat_premium, seat.number, status, Formatters.price(seat.extraPrice))
    } else {
        stringResource(R.string.cd_seat, seat.number, status)
    }
    Box(
        modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .border(
                1.dp,
                when {
                    isSelected || !seat.isAvailable -> Color.Transparent
                    seat.isPremium -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.outline
                },
                MaterialTheme.shapes.small
            )
            .toggleable(
                value = isSelected,
                enabled = seat.isAvailable,
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Text(
                seat.number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else if (seat.isPremium && seat.isAvailable) {
            Text("₹", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

