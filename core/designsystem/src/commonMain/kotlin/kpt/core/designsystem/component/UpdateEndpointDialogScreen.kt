/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.generated.resources.Res
import kpt.core.designsystem.generated.resources.core_designsystem_cancel
import kpt.core.designsystem.generated.resources.core_designsystem_dialog_action_ok
import kpt.core.designsystem.generated.resources.core_designsystem_enter_base_url
import kpt.core.designsystem.generated.resources.core_designsystem_enter_tenant
import kpt.core.designsystem.generated.resources.core_designsystem_pref_base_url_title
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// TODO: Remove it during settings new screen ui someone implemented it wrong.
// It should not be in design system
@Composable
fun UpdateEndpointDialogScreen(
    initialBaseURL: String?,
    initialTenant: String?,
    onDismissRequest: () -> Unit,
    handleEndpointUpdate: (baseURL: String, tenant: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var baseURL by rememberSaveable { mutableStateOf(initialBaseURL) }
    var tenant by rememberSaveable { mutableStateOf(initialTenant) }

    Dialog(
        onDismissRequest = { onDismissRequest.invoke() },
    ) {
        Card {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(DesignToken.padding.largeIncreased),
            ) {
                Text(text = stringResource(Res.string.core_designsystem_pref_base_url_title))
                Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                baseURL?.let {
                    OutlinedTextField(
                        value = it,
                        onValueChange = { baseURL = it },
                        label = { Text(text = stringResource(Res.string.core_designsystem_enter_base_url)) },
                    )
                }

                Spacer(modifier = Modifier.height(KptTheme.spacing.sm))

                tenant?.let {
                    OutlinedTextField(
                        value = it,
                        onValueChange = { tenant = it },
                        label = { Text(text = stringResource(Res.string.core_designsystem_enter_tenant)) },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest.invoke() },
                    ) {
                        Text(text = stringResource(Res.string.core_designsystem_cancel))
                    }
                    TextButton(
                        onClick = {
                            if (baseURL != null && tenant != null) {
                                handleEndpointUpdate.invoke(baseURL ?: "", tenant ?: "")
                            }
                        },
                    ) {
                        Text(text = stringResource(Res.string.core_designsystem_dialog_action_ok))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun UpdateEndpointDialogScreenPreview() {
    KptTheme {
        UpdateEndpointDialogScreen(
            initialBaseURL = "https://demo.mifos.org",
            initialTenant = "default",
            onDismissRequest = {},
            handleEndpointUpdate = { _, _ -> },
        )
    }
}
