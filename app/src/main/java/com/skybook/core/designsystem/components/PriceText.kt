package com.skybook.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.extended
import com.skybook.core.util.Formatters

/** Shows an amount in INR using the orange price colour. */
@Composable
fun PriceText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
) {
    Text(
        text = Formatters.price(amount),
        modifier = modifier,
        style = style,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.extended.price
    )
}

@Preview
@Composable
private fun PriceTextPreview() {
    SkyBookTheme { PriceText(amount = 4599.0) }
}
