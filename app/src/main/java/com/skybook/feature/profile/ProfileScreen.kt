package com.skybook.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skybook.R
import com.skybook.core.designsystem.components.PrimaryButton
import com.skybook.core.designsystem.components.SectionCard
import com.skybook.core.designsystem.theme.Spacing
import com.skybook.feature.booking.genderLabel

/** Simple Phase 1 profile: name/email/phone stored on device, plus saved passengers. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val passengers by viewModel.savedPassengers.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_profile)) }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = form.name.split(" ").filter { it.isNotBlank() }.take(2)
                        .joinToString("") { it.first().uppercase() }
                    if (initials.isNotEmpty()) {
                        Text(initials, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(Modifier.width(Spacing.lg))
                Column {
                    Text(
                        form.name.ifBlank { stringResource(R.string.guest_traveller) },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (form.email.isNotBlank()) {
                        Text(form.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            SectionCard(title = stringResource(R.string.your_details)) {
                OutlinedTextField(
                    value = form.name,
                    onValueChange = { v -> viewModel.update { it.copy(name = v) } },
                    label = { Text(stringResource(R.string.full_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.email,
                    onValueChange = { v -> viewModel.update { it.copy(email = v.trim()) } },
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
                )
                OutlinedTextField(
                    value = form.phone,
                    onValueChange = { v -> viewModel.update { it.copy(phone = v.filter(Char::isDigit).take(10)) } },
                    label = { Text(stringResource(R.string.phone)) },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
                )
                Spacer(Modifier.size(Spacing.lg))
                PrimaryButton(
                    text = if (form.saved) stringResource(R.string.saved) else stringResource(R.string.save),
                    onClick = viewModel::save,
                    enabled = !form.saved
                )
                Text(
                    stringResource(R.string.profile_prefill_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
            }

            SectionCard(title = stringResource(R.string.saved_passengers)) {
                if (passengers.isEmpty()) {
                    Text(
                        stringResource(R.string.no_saved_passengers),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    passengers.forEach { p ->
                        Row(Modifier.padding(vertical = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(Spacing.md))
                            Column {
                                Text(p.fullName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    stringResource(R.string.age_gender, p.age.toString(), genderLabel(p.gender)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    stringResource(R.string.phase1_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
