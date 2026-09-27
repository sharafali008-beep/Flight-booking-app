package com.skybook.feature.booking

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.FlightTimeline
import com.skybook.core.designsystem.components.LabelValueRow
import com.skybook.core.designsystem.components.PrimaryButton
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.designsystem.theme.extended
import com.skybook.core.util.Formatters

/** Step 6: success animation, PNR and trip summary. */
@Composable
fun ConfirmationScreen(
    viewModel: BookingViewModel,
    onGoToTrips: () -> Unit,
    onGoHome: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val booking = state.confirmedBooking ?: return

    // Don't let "back" return to the payment screen.
    BackHandler(onBack = onGoHome)

    // Success animation: the tick pops in with a bounce, then the text fades in.
    val scale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        contentAlpha.animateTo(1f, tween(400))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Spacer(Modifier.height(Spacing.xl))
        Box(
            Modifier
                .size(96.dp)
                .scale(scale.value)
                .background(MaterialTheme.extended.success, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(56.dp)
            )
        }
        Column(
            Modifier.alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Text(
                stringResource(R.string.booking_confirmed),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.booking_confirmed_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            SectionCard {
                Text(
                    stringResource(R.string.booking_reference),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Text(
                    booking.pnr,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            SectionCard(title = stringResource(R.string.trip_summary)) {
                Text(
                    "${booking.flight.airlineName} · ${booking.flight.flightNumber}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    Formatters.longDate(booking.flight.departureTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
                FlightTimeline(booking.flight)
                Spacer(Modifier.height(Spacing.md))
                LabelValueRow(
                    stringResource(R.string.travellers),
                    pluralStringResource(R.plurals.passenger_count, booking.passengers.size, booking.passengers.size)
                )
                LabelValueRow(stringResource(R.string.total_paid), Formatters.price(booking.totalPrice), emphasize = true)
            }
            PrimaryButton(text = stringResource(R.string.go_to_my_trips), onClick = onGoToTrips)
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) { Text(stringResource(R.string.back_to_home)) }
        }
    }
}
