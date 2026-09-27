package com.skybook.feature.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.EmptyFlightsState
import com.skybook.core.designsystem.components.ErrorState
import com.skybook.core.designsystem.components.FlightCard
import com.skybook.core.designsystem.components.FlightListSkeleton
import com.skybook.core.designsystem.components.MessageState
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import com.skybook.core.util.UiState
import com.skybook.data.model.Flight
import com.skybook.feature.home.cabinLabel
import androidx.compose.material.icons.outlined.FilterAltOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    onBack: () -> Unit,
    onFlightClick: (flight: Flight, passengers: Int) -> Unit,
    viewModel: SearchResultsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.route_title, state.from.code, state.to.code))
                        Text(
                            stringResource(
                                R.string.search_subtitle,
                                Formatters.shortDate(state.date),
                                pluralStringResource(R.plurals.passenger_count, state.passengers, state.passengers),
                                cabinLabel(state.cabin)
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = true }, enabled = state.allFlights is UiState.Success) {
                        BadgedBox(badge = {
                            if (state.filter.activeCount > 0) Badge { Text("${state.filter.activeCount}") }
                        }) {
                            Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.filters))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Sort chips (FR-07)
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                SortOption.entries.forEach { option ->
                    FilterChip(
                        selected = state.sort == option,
                        onClick = { viewModel.setSort(option) },
                        label = { Text(sortLabel(option)) }
                    )
                }
            }

            when (val all = state.allFlights) {
                UiState.Loading -> FlightListSkeleton(Modifier.padding(horizontal = Spacing.lg))
                UiState.Empty -> EmptyFlightsState(actionLabel = stringResource(R.string.change_search), onAction = onBack)
                is UiState.Error -> ErrorState(onRetry = viewModel::load)
                is UiState.Success -> {
                    val flights = state.visibleFlights
                    // Result count (FR-09)
                    Text(
                        pluralStringResource(R.plurals.flights_found, flights.size, flights.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xs)
                    )
                    if (flights.isEmpty() && all.data.isNotEmpty()) {
                        MessageState(
                            icon = Icons.Outlined.FilterAltOff,
                            title = stringResource(R.string.no_filter_match_title),
                            message = stringResource(R.string.no_filter_match_message),
                            actionLabel = stringResource(R.string.clear_filters),
                            onAction = viewModel::clearFilters
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(flights, key = { it.id }) { flight ->
                                FlightCard(flight = flight, onClick = { onFlightClick(flight, state.passengers) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        FilterSheet(
            current = state.filter,
            airlines = state.airlines,
            priceBounds = state.priceBounds,
            onApply = {
                viewModel.setFilter(it)
                showFilters = false
            },
            onDismiss = { showFilters = false }
        )
    }
}

@Composable
private fun sortLabel(option: SortOption): String = when (option) {
    SortOption.CHEAPEST -> stringResource(R.string.sort_cheapest)
    SortOption.FASTEST -> stringResource(R.string.sort_fastest)
    SortOption.EARLIEST -> stringResource(R.string.sort_earliest)
}
