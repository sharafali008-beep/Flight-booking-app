package com.skybook.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skybook.R
import com.skybook.core.designsystem.components.PrimaryButton
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.data.mock.MockFlightData
import com.skybook.data.model.Airport
import com.skybook.data.model.CabinClass
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** From / To / Date / Passengers / Class panel at the top of the Home screen (FR-03, FR-04). */
@Composable
fun SearchPanel(
    form: SearchForm,
    airports: List<Airport>,
    onFromChange: (Airport) -> Unit,
    onToChange: (Airport) -> Unit,
    onSwap: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onPassengersChange: (Int) -> Unit,
    onCabinChange: (CabinClass) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Which picker dialog is open. rememberSaveable keeps it open across rotation.
    var pickingFrom by rememberSaveable { mutableStateOf(false) }
    var pickingTo by rememberSaveable { mutableStateOf(false) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(Modifier.padding(Spacing.lg)) {
            Box {
                Column {
                    AirportField(stringResource(R.string.from), form.from) { pickingFrom = true }
                    HorizontalDivider(Modifier.padding(vertical = Spacing.xs))
                    AirportField(stringResource(R.string.to), form.to) { pickingTo = true }
                }
                FilledTonalIconButton(
                    onClick = onSwap,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = stringResource(R.string.cd_swap_airports))
                }
            }
            HorizontalDivider(Modifier.padding(vertical = Spacing.xs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.Button) { pickingDate = true }
                        .padding(vertical = Spacing.sm)
                ) {
                    FieldLabel(stringResource(R.string.departure_date))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.padding(end = Spacing.xs),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(Formatters.shortDate(form.date), style = MaterialTheme.typography.titleMedium)
                    }
                }
                PassengerStepper(form.passengers, onPassengersChange)
            }
            Spacer(Modifier.height(Spacing.md))
            CabinSelector(form.cabin, onCabinChange)
            Spacer(Modifier.height(Spacing.lg))
            if (!form.isValid) {
                Text(
                    stringResource(R.string.error_same_airports),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
            }
            PrimaryButton(
                text = stringResource(R.string.search_flights),
                onClick = onSearch,
                enabled = form.isValid
            )
        }
    }

    if (pickingFrom) {
        AirportPickerDialog(
            title = stringResource(R.string.select_origin),
            airports = airports,
            onSelect = { onFromChange(it); pickingFrom = false },
            onDismiss = { pickingFrom = false }
        )
    }
    if (pickingTo) {
        AirportPickerDialog(
            title = stringResource(R.string.select_destination),
            airports = airports,
            onSelect = { onToChange(it); pickingTo = false },
            onDismiss = { pickingTo = false }
        )
    }
    if (pickingDate) {
        TravelDatePickerDialog(
            selected = form.date,
            onSelect = { onDateChange(it); pickingDate = false },
            onDismiss = { pickingDate = false }
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun AirportField(label: String, airport: Airport, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Spacing.sm, horizontal = Spacing.xs)
            .padding(end = 56.dp) // leave room for the swap button
    ) {
        FieldLabel(label)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(airport.code, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(Spacing.sm))
            Text(
                airport.city,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun PassengerStepper(count: Int, onChange: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FieldLabel(stringResource(R.string.passengers))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(-1) }, enabled = count > HomeViewModel.MIN_PASSENGERS) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.cd_remove_passenger))
            }
            Text(
                pluralStringResource(R.plurals.passenger_count, count, count),
                style = MaterialTheme.typography.titleSmall
            )
            IconButton(onClick = { onChange(1) }, enabled = count < HomeViewModel.MAX_PASSENGERS) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_passenger))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CabinSelector(selected: CabinClass, onSelect: (CabinClass) -> Unit) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        CabinClass.entries.forEachIndexed { index, cabin ->
            SegmentedButton(
                selected = cabin == selected,
                onClick = { onSelect(cabin) },
                shape = SegmentedButtonDefaults.itemShape(index, CabinClass.entries.size),
            ) {
                Text(cabinLabel(cabin))
            }
        }
    }
}

@Composable
fun cabinLabel(cabin: CabinClass): String = when (cabin) {
    CabinClass.ECONOMY -> stringResource(R.string.cabin_economy)
    CabinClass.BUSINESS -> stringResource(R.string.cabin_business)
}

@Composable
private fun AirportPickerDialog(
    title: String,
    airports: List<Airport>,
    onSelect: (Airport) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(airports, key = { it.code }) { airport ->
                    ListItem(
                        headlineContent = { Text("${airport.city} (${airport.code})") },
                        supportingContent = { Text(airport.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.clickable { onSelect(airport) }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        modifier = Modifier.padding(vertical = Spacing.xl)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelDatePickerDialog(
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = LocalDate.now()
    val lastDay = today.plusDays(MockFlightData.BOOKING_WINDOW_DAYS - 1L)
    // The Material date picker works with UTC milliseconds.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate()
                return !date.isBefore(today) && !date.isAfter(lastDay)
            }

            override fun isSelectableYear(year: Int) = year in today.year..lastDay.year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let {
                    onSelect(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                } ?: onDismiss()
            }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    ) {
        DatePicker(state = state)
    }
}

@Preview
@Composable
private fun SearchPanelPreview() {
    SkyBookTheme {
        SearchPanel(
            form = SearchForm(MockFlightData.airports[0], MockFlightData.airports[1], LocalDate.now(), 2),
            airports = MockFlightData.airports,
            onFromChange = {}, onToChange = {}, onSwap = {}, onDateChange = {},
            onPassengersChange = {}, onCabinChange = {}, onSearch = {},
            modifier = Modifier.padding(Spacing.lg)
        )
    }
}

