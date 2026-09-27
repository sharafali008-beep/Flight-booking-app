package com.skybook.feature.booking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.skybook.R
import com.skybook.core.designsystem.components.ErrorState
import com.skybook.core.designsystem.components.PriceBar

const val BOOKING_TOTAL_STEPS = 5

/**
 * Shared layout for booking steps: top bar, "Step X of 5" progress,
 * the step's content, and the live total with a Continue button at the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStepScaffold(
    state: BookingUiState,
    title: String,
    step: Int,
    buttonText: String,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
    buttonLoading: Boolean = false,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        // Keeps the Continue bar above the on-screen keyboard.
        modifier = Modifier.imePadding(),
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                        }
                    },
                    actions = { actions() }
                )
                com.skybook.core.designsystem.components.StepProgress(step, BOOKING_TOTAL_STEPS, title)
            }
        },
        bottomBar = {
            if (state.flight != null) {
                PriceBar(
                    total = state.price.total,
                    passengers = state.passengerCount,
                    buttonText = buttonText,
                    onClick = onContinue,
                    loading = buttonLoading,
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.loadFailed -> ErrorState(onRetry = onRetry, modifier = Modifier.align(Alignment.Center))
                else -> Column(Modifier.fillMaxSize(), content = content)
            }
        }
    }
}
