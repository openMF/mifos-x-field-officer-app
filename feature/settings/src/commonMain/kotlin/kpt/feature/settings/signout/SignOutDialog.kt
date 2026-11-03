/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.signout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_sign_out
import kpt.feature.settings.generated.resources.feature_settings_sign_out_cancel
import kpt.feature.settings.generated.resources.feature_settings_sign_out_clear_data
import kpt.feature.settings.generated.resources.feature_settings_sign_out_clear_data_detail
import kpt.feature.settings.generated.resources.feature_settings_sign_out_confirm
import kpt.feature.settings.generated.resources.feature_settings_sign_out_confirm_clear
import kpt.feature.settings.generated.resources.feature_settings_sign_out_failed
import kpt.feature.settings.generated.resources.feature_settings_sign_out_keep_data
import kpt.feature.settings.generated.resources.feature_settings_sign_out_keep_data_detail
import org.jetbrains.compose.resources.stringResource

/** The sign-out choice, shown while [SignOutState.confirming]. Stateless — [SignOutViewModel] owns it. */
@Composable
fun SignOutDialog(state: SignOutState, onAction: (SignOutAction) -> Unit) {
    if (state.confirming) {
        AlertDialog(
            onDismissRequest = { onAction(SignOutAction.Cancelled) },
            title = { Text(stringResource(Res.string.feature_settings_sign_out)) },
            text = { SignOutChoices(state = state, onAction = onAction) },
            confirmButton = {
                Button(
                    onClick = { onAction(SignOutAction.Confirmed) },
                    enabled = !state.isSigningOut,
                    modifier = Modifier.testTag(SignOutTestTags.CONFIRM),
                ) {
                    Text(
                        stringResource(
                            if (state.mode == SignOutMode.SessionAndData) {
                                Res.string.feature_settings_sign_out_confirm_clear
                            } else {
                                Res.string.feature_settings_sign_out_confirm
                            },
                        ),
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onAction(SignOutAction.Cancelled) }) {
                    Text(stringResource(Res.string.feature_settings_sign_out_cancel))
                }
            },
        )
    }
}

/**
 * The two things sign-out can mean, as one choice. Radio buttons rather than checkboxes: exactly one
 * applies, and "neither" or "both" are not sign-outs. The destructive option spells out that it
 * includes unsynced work, because that is the one loss the officer cannot get back from the server.
 */
@Composable
private fun SignOutChoices(state: SignOutState, onAction: (SignOutAction) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.selectableGroup(),
    ) {
        SignOutOption(
            selected = state.mode == SignOutMode.SessionAndData,
            title = stringResource(Res.string.feature_settings_sign_out_clear_data),
            description = stringResource(Res.string.feature_settings_sign_out_clear_data_detail),
            onSelect = { onAction(SignOutAction.ModeSelected(SignOutMode.SessionAndData)) },
            modifier = Modifier.testTag(SignOutTestTags.CLEAR_DATA),
        )
        SignOutOption(
            selected = state.mode == SignOutMode.SessionOnly,
            title = stringResource(Res.string.feature_settings_sign_out_keep_data),
            description = stringResource(Res.string.feature_settings_sign_out_keep_data_detail),
            onSelect = { onAction(SignOutAction.ModeSelected(SignOutMode.SessionOnly)) },
            modifier = Modifier.testTag(SignOutTestTags.KEEP_DATA),
        )
        if (state.failed) {
            Text(
                text = stringResource(Res.string.feature_settings_sign_out_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(SignOutTestTags.FAILED),
            )
        }
    }
}

@Composable
private fun SignOutOption(
    selected: Boolean,
    title: String,
    description: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        // onClick = null: the whole row is the touch target (and the one TalkBack announces).
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
