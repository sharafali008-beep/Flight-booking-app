package com.skybook.feature.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.core.util.Formatters
import androidx.compose.material3.OutlinedTextField

/** Step 5: mock payment. Always succeeds after a short delay (Phase 1). */
@Composable
fun PaymentScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onPaid: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // When the booking is saved, move to the confirmation screen.
    LaunchedEffect(state.confirmedBooking) {
        if (state.confirmedBooking != null) onPaid()
    }

    BookingStepScaffold(
        state = state,
        title = stringResource(R.string.step_payment),
        step = 5,
        buttonText = stringResource(R.string.pay_amount, Formatters.price(state.price.total)),
        buttonLoading = state.isPaying,
        onBack = { if (!state.isPaying) onBack() },
        onContinue = viewModel::pay,
        onRetry = viewModel::load,
    ) {
        val payment = state.payment
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                MethodCard(
                    icon = Icons.Outlined.CreditCard,
                    title = stringResource(R.string.pay_card),
                    selected = payment.method == PaymentMethod.CARD,
                    onClick = { viewModel.updatePayment { it.copy(method = PaymentMethod.CARD) } }
                )
                MethodCard(
                    icon = Icons.Outlined.QrCode2,
                    title = stringResource(R.string.pay_upi),
                    selected = payment.method == PaymentMethod.UPI,
                    onClick = { viewModel.updatePayment { it.copy(method = PaymentMethod.UPI) } }
                )
            }

            SectionCard {
                when (payment.method) {
                    PaymentMethod.CARD -> {
                        FormField(
                            value = payment.cardNumber,
                            onValueChange = { v ->
                                viewModel.updatePayment { it.copy(cardNumber = v.filter(Char::isDigit).take(16)) }
                            },
                            label = stringResource(R.string.card_number),
                            error = payment.cardNumberError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        )
                        FormField(
                            value = payment.cardName,
                            onValueChange = { v -> viewModel.updatePayment { it.copy(cardName = v) } },
                            label = stringResource(R.string.name_on_card),
                            error = payment.cardNameError,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Next
                            ),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                            FormField(
                                value = payment.expiry,
                                onValueChange = { v -> viewModel.updatePayment { it.copy(expiry = formatExpiry(v)) } },
                                label = stringResource(R.string.expiry),
                                error = payment.expiryError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = payment.cvv,
                                onValueChange = { v ->
                                    viewModel.updatePayment { it.copy(cvv = v.filter(Char::isDigit).take(3)) }
                                },
                                label = { Text(stringResource(R.string.cvv)) },
                                isError = payment.cvvError != null,
                                supportingText = payment.cvvError?.let { { Text(stringResource(it)) } },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.weight(1f).padding(vertical = Spacing.xs)
                            )
                        }
                    }
                    PaymentMethod.UPI -> FormField(
                        value = payment.upiId,
                        onValueChange = { v -> viewModel.updatePayment { it.copy(upiId = v.trim()) } },
                        label = stringResource(R.string.upi_id),
                        error = payment.upiError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    stringResource(R.string.demo_payment_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** "0327" -> "03/27" as the user types. */
private fun formatExpiry(input: String): String {
    val digits = input.filter(Char::isDigit).take(4)
    return if (digits.length > 2) digits.substring(0, 2) + "/" + digits.substring(2) else digits
}

@Composable
private fun MethodCard(icon: ImageVector, title: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Spacer(Modifier.width(Spacing.sm))
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(Spacing.md))
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
    }
}
