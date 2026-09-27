package com.skybook.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skybook.R
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.Spacing

/** Sticky bar at the bottom: total price + main button. Used on Flight Details and every booking step. */
@Composable
fun PriceBar(
    total: Double,
    passengers: Int,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                PriceText(total)
                Text(
                    pluralStringResource(R.plurals.total_for_passengers, passengers, passengers),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PrimaryButton(
                text = buttonText,
                onClick = onClick,
                enabled = enabled,
                loading = loading,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview
@Composable
private fun PriceBarPreview() {
    SkyBookTheme { PriceBar(total = 10_298.0, passengers = 2, buttonText = "Continue", onClick = {}) }
}
