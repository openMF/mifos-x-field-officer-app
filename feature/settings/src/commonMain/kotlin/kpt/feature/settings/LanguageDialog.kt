/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.model.user.LanguageConfig
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_dismiss_dialog_button_text
import kpt.feature.settings.generated.resources.feature_settings_language_preference
import kpt.feature.settings.generated.resources.feature_settings_loading
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    LanguageDialog(
        onDismiss = onDismiss,
        settingsState = settingsState,
        onRetry = viewModel::onRetry,
        onChangeLanguage = viewModel::updateLanguage,
    )
}

@Composable
fun LanguageDialog(
    settingsState: ScreenState<UserEditableSettings>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier.fillMaxWidth(0.8f),
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = stringResource(resource = Res.string.feature_settings_language_preference),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            HorizontalDivider()
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // Store5 read surface — same wrapper as every other read screen. `loading` keeps
                // the dialog's compact copy (a full-screen spinner would be wrong in an
                // AlertDialog); Error/NoNetwork now render with a retry wired to the store,
                // which the previous hand-rolled Loading|Success pair could not express.
                ScreenContent(
                    state = settingsState,
                    onRetry = onRetry,
                    loading = {
                        Text(
                            text = stringResource(resource = Res.string.feature_settings_loading),
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    },
                ) { settings, _ ->
                    LanguagePanel(
                        currentLanguage = settings.language,
                        onChangeLanguage = onChangeLanguage,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.padding(horizontal = 8.dp),
            ) {
                Text(
                    text = stringResource(resource = Res.string.feature_settings_dismiss_dialog_button_text),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun LanguagePanel(currentLanguage: LanguageConfig, onChangeLanguage: (language: LanguageConfig) -> Unit) {
    Column(Modifier.selectableGroup()) {
        LanguageConfig.entries.forEach { language ->
            LanguageChooserRow(
                text = language.text,
                selected = currentLanguage == language,
                onClick = { onChangeLanguage(language) },
            )
        }
    }
}

@Composable
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
