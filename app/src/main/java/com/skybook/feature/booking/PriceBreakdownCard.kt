package com.skybook.feature.booking

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.skybook.R
import com.skybook.core.designsystem.components.LabelValueRow
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.SkyBookTheme
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import androidx.compose.foundation.layout.padding

/** base fare + seats + add-ons + taxes = total */
@Composable
fun PriceBreakdownCard(
    farePerPassenger: Double,
    passengers: Int,
    baseFare: Double,
    seatsTotal: Double,
    addOnsTotal: Double,
    taxes: Double,
    total: Double,
    modifier: Modifier = Modifier,
) {
    SectionCard(modifier = modifier, title = stringResource(R.string.price_breakdown)) {
        LabelValueRow(
            stringResource(R.string.base_fare_calc, Formatters.price(farePerPassenger), passengers),
            Formatters.price(baseFare)
        )
        LabelValueRow(stringResource(R.string.seat_charges), Formatters.price(seatsTotal))
        LabelValueRow(stringResource(R.string.addons_total), Formatters.price(addOnsTotal))
        LabelValueRow(stringResource(R.string.taxes_fees), Formatters.price(taxes))
        HorizontalDivider(Modifier.padding(vertical = Spacing.sm))
        LabelValueRow(stringResource(R.string.total_amount), Formatters.price(total), emphasize = true)
    }
}

@Composable
fun PriceBreakdownCard(price: PriceBreakdown, modifier: Modifier = Modifier) = PriceBreakdownCard(
    price.farePerPassenger, price.passengers, price.baseFare, price.seatsTotal,
    price.addOnsTotal, price.taxes, price.total, modifier
)

@Preview
@Composable
private fun PriceBreakdownPreview() {
    SkyBookTheme {
        PriceBreakdownCard(PriceBreakdown(4599.0, 2, 9198.0, 450.0, 750.0, 1104.0))
    }
}
