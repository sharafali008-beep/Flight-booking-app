package com.skybook.feature.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.FlightTimeline
import com.skybook.core.designsystem.components.LabelValueRow
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.data.model.BaggageOption
import com.skybook.feature.flightdetails.fareName
import com.skybook.feature.home.cabinLabel

/** Step 4: summary of everything before paying. */
@Composable
fun ReviewBookingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BookingStepScaffold(
        state = state,
        title = stringResource(R.string.step_review),
        step = 4,
        buttonText = stringResource(R.string.proceed_to_pay),
        onBack = onBack,
        onContinue = onContinue,
        onRetry = viewModel::load,
    ) {
        val flight = state.flight ?: return@BookingStepScaffold
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            SectionCard(title = stringResource(R.string.flight)) {
                Text(
                    "${flight.airlineName} · ${flight.flightNumber}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "${Formatters.longDate(flight.departureTime)} · ${cabinLabel(flight.cabinClass)} · " +
                        (state.fare?.let { fareName(it.type) } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
                FlightTimeline(flight)
            }

            SectionCard(title = stringResource(R.string.travellers)) {
                state.passengers.forEachIndexed { index, p ->
                    if (index > 0) HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Text(p.fullName, style = MaterialTheme.typography.titleSmall)
                            Text(
                                stringResource(R.string.age_gender, p.age, genderLabel(p.gender?.name.orEmpty())),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column {
                            Text(
                                stringResource(
                                    R.string.seat_label,
                                    state.selectedSeats.getOrNull(index) ?: stringResource(R.string.seat_auto)
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                stringResource(R.string.meal_label, mealLabel(p.meal)),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            SectionCard(title = stringResource(R.string.step_addons)) {
                LabelValueRow(
                    stringResource(R.string.extra_baggage),
                    if (state.baggage == BaggageOption.NONE) stringResource(R.string.none)
                    else stringResource(R.string.extra_kg, state.baggage.extraKg)
                )
                val meals = state.passengers.count { it.meal.price > 0 }
                LabelValueRow(stringResource(R.string.meals), "$meals")
            }

            PriceBreakdownCard(state.price)
        }
    }
}
