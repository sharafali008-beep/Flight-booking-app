package com.skybook.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.skybook.R
import com.skybook.core.designsystem.theme.SkyBookTheme

/**
 * Loads the airline logo from the internet with Coil.
 * While loading (or if offline) it shows the airline code in a coloured box instead.
 */
@Composable
fun AirlineLogo(
    logoUrl: String,
    airlineCode: String,
    airlineName: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val shape = RoundedCornerShape(8.dp)
    SubcomposeAsyncImage(
        model = logoUrl,
        contentDescription = stringResource(R.string.cd_airline_logo, airlineName),
        modifier = modifier.size(size).clip(shape),
        loading = { LogoFallback(airlineCode) },
        error = { LogoFallback(airlineCode) },
    )
}

@Composable
private fun LogoFallback(code: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview
@Composable
private fun AirlineLogoPreview() {
    SkyBookTheme { AirlineLogo(logoUrl = "", airlineCode = "6E", airlineName = "IndiGo") }
}
