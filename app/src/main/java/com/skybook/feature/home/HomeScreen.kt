package com.skybook.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.EmptyFlightsState
import com.skybook.core.designsystem.components.ErrorState
import com.skybook.core.designsystem.components.FlightCard
import com.skybook.core.designsystem.components.FlightCardSkeleton
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.core.util.UiState
import com.skybook.data.model.CabinClass
import com.skybook.data.model.Flight
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSearch: (from: String, to: String, date: LocalDate, passengers: Int, cabin: CabinClass) -> Unit,
    onFlightClick: (flight: Flight, passengers: Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form = state.form

    // The bottom navigation bar already handles the system bar at the bottom.
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = Spacing.xl)
            ) {
                item(key = "header") {
                    Box {
                        // Blue header behind the top half of the search card.
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                        Column(Modifier.statusBarsPadding().padding(Spacing.lg)) {
                            Text(
                                stringResource(R.string.home_greeting),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                stringResource(R.string.home_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = Spacing.lg)
                            )
                            SearchPanel(
                                form = form,
                                airports = viewModel.airports,
                                onFromChange = viewModel::setFrom,
                                onToChange = viewModel::setTo,
                                onSwap = viewModel::swapAirports,
                                onDateChange = viewModel::setDate,
                                onPassengersChange = viewModel::changePassengers,
                                onCabinChange = viewModel::setCabin,
                                onSearch = {
                                    onSearch(form.from.code, form.to.code, form.date, form.passengers, form.cabin)
                                }
                            )
                        }
                    }
                }
                item(key = "list_title") {
                    Text(
                        stringResource(R.string.home_flights_on, Formatters.shortDate(form.date)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                    )
                }
                when (val flights = state.flights) {
                    UiState.Loading -> items(4) {
                        FlightCardSkeleton(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm))
                    }
                    UiState.Empty -> item { EmptyFlightsState() }
                    is UiState.Error -> item { ErrorState(onRetry = { viewModel.loadFlights() }) }
                    is UiState.Success -> items(flights.data, key = { it.id }) { flight ->
                        FlightCard(
                            flight = flight,
                            onClick = { onFlightClick(flight, form.passengers) },
                            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm)
                        )
                    }
                }
            }
        }
    }
}
