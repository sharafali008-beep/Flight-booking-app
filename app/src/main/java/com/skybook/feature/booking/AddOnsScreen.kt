package com.skybook.feature.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.data.model.BaggageOption
import com.skybook.data.model.MealOption

/** Step 3 (optional): extra baggage and meals. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOnsScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BookingStepScaffold(
        state = state,
        title = stringResource(R.string.step_addons),
        step = 3,
        buttonText = stringResource(R.string.continue_label),
        onBack = onBack,
        onContinue = onContinue,
        onRetry = viewModel::load,
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            SectionCard(title = stringResource(R.string.extra_baggage)) {
                Text(
                    stringResource(R.string.included_baggage, state.fare?.baggageKg ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.sm)
                )
                Column(Modifier.selectableGroup()) {
                    BaggageOption.entries.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .selectable(
                                    selected = state.baggage == option,
                                    onClick = { viewModel.setBaggage(option) },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = state.baggage == option, onClick = null)
                            Spacer(Modifier.width(Spacing.md))
                            Text(
                                if (option == BaggageOption.NONE) stringResource(R.string.no_extra_baggage)
                                else stringResource(R.string.extra_kg, option.extraKg),
                                modifier = Modifier.weight(1f)
                            )
                            if (option.price > 0) Text(Formatters.price(option.price))
                        }
                    }
                }
            }

            SectionCard(title = stringResource(R.string.meals)) {
                state.passengers.forEachIndexed { index, passenger ->
                    Text(
                        passenger.fullName.ifBlank { stringResource(R.string.passenger_number, index + 1) },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = if (index == 0) 0.dp else Spacing.md)
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        MealOption.entries.forEach { meal ->
                            FilterChip(
                                selected = passenger.meal == meal,
                                onClick = { viewModel.setMeal(index, meal) },
                                label = { Text(mealLabel(meal, withPrice = true)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun mealLabel(meal: MealOption, withPrice: Boolean = false): String {
    val name = when (meal) {
        MealOption.NONE -> stringResource(R.string.meal_none)
        MealOption.VEG -> stringResource(R.string.meal_veg)
        MealOption.NON_VEG -> stringResource(R.string.meal_non_veg)
    }
    return if (withPrice && meal.price > 0) "$name · ${Formatters.price(meal.price)}" else name
}
