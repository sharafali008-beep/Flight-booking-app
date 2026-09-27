package com.skybook.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skybook.R
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.Spacing

/** Centered icon + title + message, with an optional button. Used for empty and error states. */
@Composable
fun MessageState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(Spacing.lg))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.lg))
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun EmptyFlightsState(modifier: Modifier = Modifier, onAction: (() -> Unit)? = null, actionLabel: String? = null) {
    MessageState(
        icon = Icons.Outlined.TravelExplore,
        title = stringResource(R.string.empty_flights_title),
        message = stringResource(R.string.empty_flights_message),
        modifier = modifier,
        actionLabel = actionLabel,
        onAction = onAction
    )
}

@Composable
fun ErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    MessageState(
        icon = Icons.Outlined.CloudOff,
        title = stringResource(R.string.error_title),
        message = stringResource(R.string.error_message),
        modifier = modifier,
        actionLabel = stringResource(R.string.retry),
        onAction = onRetry
    )
}

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() {
    SkyBookTheme { EmptyFlightsState() }
}

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() {
    SkyBookTheme { ErrorState(onRetry = {}) }
}
