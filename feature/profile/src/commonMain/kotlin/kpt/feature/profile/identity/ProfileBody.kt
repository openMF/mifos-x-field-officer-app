/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.feature.profile.TestTags
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_office
import kpt.feature.profile.generated.resources.feature_profile_server
import kpt.feature.profile.generated.resources.feature_profile_sign_out
import kpt.feature.profile.generated.resources.feature_profile_sign_out_cancel
import kpt.feature.profile.generated.resources.feature_profile_sign_out_confirm
import kpt.feature.profile.generated.resources.feature_profile_sign_out_prompt
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Profile tab's body — signed-in officer identity and sign-out.
 *
 * Supplied through `BackboneRegistry.profileBody`, another seam that was EMPTY.
 */
@Composable
fun ProfileBody(
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { if (it is ProfileEvent.SignedOut) onSignedOut() }
    }

    ProfileBodyContent(
        state = state,
        onAction = { viewModel.actionChannel.trySend(it) },
        modifier = modifier,
    )
}

/** Stateless half — see `SearchBodyContent` for why the split exists. */
@Composable
internal fun ProfileBodyContent(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag(TestTags.Profile.BODY),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = state.username,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag(TestTags.Profile.USERNAME),
        )
        Text(
            text = stringResource(Res.string.feature_profile_office, state.officeName),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            // Which server the officer is signed in to is identity, not decoration: a field officer
            // switching between a demo and a production instance has no other way to tell.
            text = stringResource(Res.string.feature_profile_server, state.instanceUrl),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.testTag(TestTags.Profile.SERVER),
        )

        Button(
            onClick = { onAction(ProfileAction.SignOutRequested) },
            modifier = Modifier.fillMaxWidth().testTag(TestTags.Profile.SIGN_OUT),
        ) {
            Text(stringResource(Res.string.feature_profile_sign_out))
        }
    }

    if (state.confirmingSignOut) {
        AlertDialog(
            onDismissRequest = { onAction(ProfileAction.SignOutCancelled) },
            title = { Text(stringResource(Res.string.feature_profile_sign_out)) },
            text = { Text(stringResource(Res.string.feature_profile_sign_out_prompt)) },
            confirmButton = {
                Button(
                    onClick = { onAction(ProfileAction.SignOutConfirmed) },
                    modifier = Modifier.testTag(TestTags.Profile.SIGN_OUT_CONFIRM),
                ) {
                    Text(stringResource(Res.string.feature_profile_sign_out_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onAction(ProfileAction.SignOutCancelled) }) {
                    Text(stringResource(Res.string.feature_profile_sign_out_cancel))
                }
            },
        )
    }
}
