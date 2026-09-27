package com.skybook.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.skybook.R
import com.skybook.core.designsystem.components.PrimaryButton
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import androidx.compose.ui.unit.dp

/**
 * Bottom sheet with filter options (FR-08). Changes are kept locally
 * and only sent to the ViewModel when the user taps "Apply".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    current: FlightFilter,
    airlines: List<String>,
    priceBounds: ClosedFloatingPointRange<Double>?,
    onApply: (FlightFilter) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember { mutableStateOf(current) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .navigationBarsPadding()
        ) {
            Text(stringResource(R.string.filters), style = MaterialTheme.typography.titleLarge)

            SectionTitle(stringResource(R.string.filter_stops))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StopsFilter.entries.forEach { option ->
                    FilterChip(
                        selected = draft.stops == option,
                        onClick = { draft = draft.copy(stops = option) },
                        label = {
                            Text(
                                when (option) {
                                    StopsFilter.ANY -> stringResource(R.string.filter_any)
                                    StopsFilter.NON_STOP -> stringResource(R.string.non_stop)
                                    StopsFilter.ONE_STOP -> stringResource(R.string.filter_one_stop)
                                }
                            )
                        }
                    )
                }
            }

            if (airlines.isNotEmpty()) {
                SectionTitle(stringResource(R.string.filter_airlines))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    airlines.forEach { airline ->
                        val selected = airline in draft.airlines
                        FilterChip(
                            selected = selected,
                            onClick = {
                                draft = draft.copy(
                                    airlines = if (selected) draft.airlines - airline else draft.airlines + airline
                                )
                            },
                            label = { Text(airline) }
                        )
                    }
                }
            }

            SectionTitle(stringResource(R.string.filter_departure_time))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                TimeSlot.entries.forEach { slot ->
                    val selected = slot in draft.timeSlots
                    FilterChip(
                        selected = selected,
                        onClick = {
                            draft = draft.copy(
                                timeSlots = if (selected) draft.timeSlots - slot else draft.timeSlots + slot
                            )
                        },
                        label = { Text(timeSlotLabel(slot)) }
                    )
                }
            }

            if (priceBounds != null && priceBounds.start < priceBounds.endInclusive) {
                SectionTitle(stringResource(R.string.filter_price))
                val min = draft.minPrice ?: priceBounds.start
                val max = draft.maxPrice ?: priceBounds.endInclusive
                Row {
                    Text(Formatters.price(min), Modifier.weight(1f))
                    Text(Formatters.price(max))
                }
                RangeSlider(
                    value = min.toFloat()..max.toFloat(),
                    onValueChange = { range ->
                        draft = draft.copy(
                            minPrice = range.start.toDouble().takeIf { it > priceBounds.start },
                            maxPrice = range.endInclusive.toDouble().takeIf { it < priceBounds.endInclusive },
                        )
                    },
                    valueRange = priceBounds.start.toFloat()..priceBounds.endInclusive.toFloat(),
                )
            }

            Spacer(Modifier.height(Spacing.xl))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedButton(
                    onClick = { draft = FlightFilter() },
                    modifier = Modifier.weight(1f).height(52.dp)
                ) { Text(stringResource(R.string.clear_all)) }
                PrimaryButton(
                    text = stringResource(R.string.apply),
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.lg, bottom = Spacing.sm)
    )
}

@Composable
fun timeSlotLabel(slot: TimeSlot): String = when (slot) {
    TimeSlot.EARLY_MORNING -> stringResource(R.string.slot_early_morning)
    TimeSlot.MORNING -> stringResource(R.string.slot_morning)
    TimeSlot.AFTERNOON -> stringResource(R.string.slot_afternoon)
    TimeSlot.EVENING -> stringResource(R.string.slot_evening)
}
