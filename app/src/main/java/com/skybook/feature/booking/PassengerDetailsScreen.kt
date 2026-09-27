package com.skybook.feature.booking

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing

/** Step 1: one form per passenger (name, age, gender, email, phone). */
@Composable
fun PassengerDetailsScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BookingStepScaffold(
        state = state,
        title = stringResource(R.string.step_passengers),
        step = 1,
        buttonText = stringResource(R.string.continue_label),
        onBack = onBack,
        onContinue = { if (viewModel.validatePassengers()) onContinue() },
        onRetry = viewModel::load,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            itemsIndexed(state.passengers) { index, form ->
                PassengerFormCard(
                    index = index,
                    form = form,
                    onChange = { transform -> viewModel.updatePassenger(index, transform) }
                )
            }
        }
    }
}

@Composable
private fun PassengerFormCard(
    index: Int,
    form: PassengerForm,
    onChange: ((PassengerForm) -> PassengerForm) -> Unit,
) {
    val isPrimary = index == 0
    SectionCard(title = stringResource(R.string.passenger_number, index + 1)) {
        FormField(
            value = form.fullName,
            onValueChange = { v -> onChange { it.copy(fullName = v) } },
            label = stringResource(R.string.full_name),
            error = form.nameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
        )
        FormField(
            value = form.age,
            onValueChange = { v -> onChange { it.copy(age = v.filter(Char::isDigit).take(3)) } },
            label = stringResource(R.string.age),
            error = form.ageError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        )
        Text(
            stringResource(R.string.gender),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = Spacing.sm)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Gender.entries.forEach { gender ->
                FilterChip(
                    selected = form.gender == gender,
                    onClick = { onChange { it.copy(gender = gender) } },
                    label = { Text(genderLabel(gender.name)) }
                )
            }
        }
        form.genderError?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            if (isPrimary) stringResource(R.string.contact_details_required)
            else stringResource(R.string.contact_details_optional),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FormField(
            value = form.email,
            onValueChange = { v -> onChange { it.copy(email = v.trim()) } },
            label = stringResource(R.string.email),
            error = form.emailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        FormField(
            value = form.phone,
            onValueChange = { v -> onChange { it.copy(phone = v.filter(Char::isDigit).take(10)) } },
            label = stringResource(R.string.phone),
            error = form.phoneError,
            prefix = "+91 ",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        )
    }
}

@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    @StringRes error: Int?,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        singleLine = true,
        prefix = prefix?.let { { Text(it) } },
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth().padding(vertical = Spacing.xs)
    )
}

/** Gender is stored as "MALE"/"FEMALE"/"OTHER"; this shows the translated label. */
@Composable
fun genderLabel(key: String): String = when (key) {
    Gender.MALE.name -> stringResource(R.string.gender_male)
    Gender.FEMALE.name -> stringResource(R.string.gender_female)
    else -> stringResource(R.string.gender_other)
}
