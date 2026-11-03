/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kpt.core.base.designsystem.theme.KptTheme
import kpt.feature.profile.TestTags
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_cancel
import kpt.feature.profile.generated.resources.feature_profile_change_password
import kpt.feature.profile.generated.resources.feature_profile_change_password_offline
import kpt.feature.profile.generated.resources.feature_profile_hide_password
import kpt.feature.profile.generated.resources.feature_profile_new_password
import kpt.feature.profile.generated.resources.feature_profile_password_failed
import kpt.feature.profile.generated.resources.feature_profile_password_hint
import kpt.feature.profile.generated.resources.feature_profile_password_lead
import kpt.feature.profile.generated.resources.feature_profile_passwords_differ
import kpt.feature.profile.generated.resources.feature_profile_repeat_password
import kpt.feature.profile.generated.resources.feature_profile_save_password
import kpt.feature.profile.generated.resources.feature_profile_show_password
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PasswordSheet(form: PasswordForm, onAction: (ProfileAction) -> Unit) {
    // Fully expanded: a half-open sheet put Save below the fold behind the keyboard (device, 2026-10-27).
    ModalBottomSheet(
        onDismissRequest = { onAction(ProfileAction.DismissPassword) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag(TestTags.Profile.PASSWORD_SHEET),
    ) {
        PasswordSheetBody(form, onAction)
    }
}

/** The sheet's content, outside the [ModalBottomSheet] so a preview can render it. */
@Composable
internal fun PasswordSheetBody(form: PasswordForm, onAction: (ProfileAction) -> Unit) {
    val c = KptTheme.colorScheme
    Column(
        // The sheet draws to the screen edge: keep Cancel / Save above the gesture bar and the keyboard,
        // and scroll when a long server message pushes them down (device, 2026-10-27).
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KptTheme.spacing.lg)
            .padding(bottom = KptTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Text(
            stringResource(Res.string.feature_profile_change_password),
            style = KptTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            stringResource(Res.string.feature_profile_password_lead),
            style = KptTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
        )
        PasswordField(
            value = form.new,
            onValueChange = { onAction(ProfileAction.NewPassword(it)) },
            label = stringResource(Res.string.feature_profile_new_password),
            supporting = stringResource(Res.string.feature_profile_password_hint),
            isError = false,
            modifier = Modifier.testTag(TestTags.Profile.NEW_PASSWORD),
        )
        PasswordField(
            value = form.repeat,
            onValueChange = { onAction(ProfileAction.RepeatPassword(it)) },
            label = stringResource(Res.string.feature_profile_repeat_password),
            supporting = if (form.mismatch) stringResource(Res.string.feature_profile_passwords_differ) else null,
            isError = form.mismatch,
            modifier = Modifier.testTag(TestTags.Profile.REPEAT_PASSWORD),
        )
        form.error?.let { error ->
            val message = when (error) {
                is PasswordError.Server -> error.message ?: stringResource(Res.string.feature_profile_password_failed)
                PasswordError.Offline -> stringResource(Res.string.feature_profile_change_password_offline)
            }
            Text(
                message,
                style = KptTheme.typography.bodySmall,
                color = c.error,
                modifier = Modifier.testTag(TestTags.Profile.PASSWORD_ERROR),
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm, Alignment.End),
        ) {
            TextButton(
                onClick = { onAction(ProfileAction.DismissPassword) },
                modifier = Modifier.testTag(TestTags.Profile.PASSWORD_CANCEL),
            ) {
                Text(stringResource(Res.string.feature_profile_cancel))
            }
            Button(
                onClick = { onAction(ProfileAction.SubmitPassword) },
                enabled = form.canSave,
                modifier = Modifier.testTag(TestTags.Profile.PASSWORD_SAVE),
            ) {
                Text(stringResource(Res.string.feature_profile_save_password))
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supporting: String?,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = supporting?.let { { Text(it) } },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = stringResource(
                        if (visible) {
                            Res.string.feature_profile_hide_password
                        } else {
                            Res.string.feature_profile_show_password
                        },
                    ),
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}
