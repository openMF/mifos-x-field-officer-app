/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.serverconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.domain.validation.ValidationFailure
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_server_api_path
import kpt.feature.settings.generated.resources.feature_settings_server_config_title
import kpt.feature.settings.generated.resources.feature_settings_server_endpoint
import kpt.feature.settings.generated.resources.feature_settings_server_error_api_path_leading
import kpt.feature.settings.generated.resources.feature_settings_server_error_api_path_trailing
import kpt.feature.settings.generated.resources.feature_settings_server_error_hostname
import kpt.feature.settings.generated.resources.feature_settings_server_error_port_range
import kpt.feature.settings.generated.resources.feature_settings_server_error_protocol
import kpt.feature.settings.generated.resources.feature_settings_server_error_required
import kpt.feature.settings.generated.resources.feature_settings_server_error_tenant
import kpt.feature.settings.generated.resources.feature_settings_server_error_too_short
import kpt.feature.settings.generated.resources.feature_settings_server_invalid
import kpt.feature.settings.generated.resources.feature_settings_server_port
import kpt.feature.settings.generated.resources.feature_settings_server_protocol
import kpt.feature.settings.generated.resources.feature_settings_server_save
import kpt.feature.settings.generated.resources.feature_settings_server_saved
import kpt.feature.settings.generated.resources.feature_settings_server_tenant
import kpt.feature.settings.generated.resources.feature_settings_server_use_defaults
import kpt.feature.settings.generated.resources.feature_settings_server_use_localhost
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Edits the Fineract server configuration — protocol, host, API path, port, tenant.
 *
 * Ported from `6b66e8a43:feature/settings/.../updateServer/UpdateServerConfigScreen.kt`. Reachable
 * from the sign-in screen (see [serverConfigDestination] for why it sits on the root graph) and from
 * Settings once signed in.
 *
 * Field errors are rendered ON the field, from the typed [ValidationFailure] the ViewModel produces.
 * The original received a pre-localized `String?` per field out of `core/domain`, which is precisely
 * what forced Compose resources into the domain layer (spec §2).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerConfigViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.feature_settings_server_saved)
    val invalidMessage = stringResource(Res.string.feature_settings_server_invalid)

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                // Confirm, then leave — the original popped back with no confirmation, so a user
                // could not tell a successful save from a dismissed screen.
                ServerConfigEvent.Saved -> {
                    snackbarHostState.showSnackbar(savedMessage)
                    onBackClick()
                }
                ServerConfigEvent.Invalid -> snackbarHostState.showSnackbar(invalidMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_settings_server_config_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag(ServerConfigTestTags.BACK)) {
                        Icon(MifosIcons.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .testTag(ServerConfigTestTags.SCREEN),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Field(
                value = state.config.protocol,
                onChange = { viewModel.actionChannel.trySend(ServerConfigAction.ProtocolChanged(it)) },
                label = stringResource(Res.string.feature_settings_server_protocol),
                failure = state.failures[ServerConfigField.Protocol],
                tag = ServerConfigTestTags.PROTOCOL,
            )
            Field(
                value = state.config.endPoint,
                onChange = { viewModel.actionChannel.trySend(ServerConfigAction.EndPointChanged(it)) },
                label = stringResource(Res.string.feature_settings_server_endpoint),
                failure = state.failures[ServerConfigField.EndPoint],
                tag = ServerConfigTestTags.ENDPOINT,
            )
            Field(
                value = state.config.apiPath,
                onChange = { viewModel.actionChannel.trySend(ServerConfigAction.ApiPathChanged(it)) },
                label = stringResource(Res.string.feature_settings_server_api_path),
                failure = state.failures[ServerConfigField.ApiPath],
                tag = ServerConfigTestTags.API_PATH,
            )
            Field(
                value = state.config.port,
                onChange = { viewModel.actionChannel.trySend(ServerConfigAction.PortChanged(it)) },
                label = stringResource(Res.string.feature_settings_server_port),
                failure = state.failures[ServerConfigField.Port],
                tag = ServerConfigTestTags.PORT,
                keyboardType = KeyboardType.Number,
            )
            Field(
                value = state.config.tenant,
                onChange = { viewModel.actionChannel.trySend(ServerConfigAction.TenantChanged(it)) },
                label = stringResource(Res.string.feature_settings_server_tenant),
                failure = state.failures[ServerConfigField.Tenant],
                tag = ServerConfigTestTags.TENANT,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { viewModel.actionChannel.trySend(ServerConfigAction.UseDefaults) },
                    modifier = Modifier.weight(1f).testTag(ServerConfigTestTags.USE_DEFAULTS),
                ) {
                    Text(stringResource(Res.string.feature_settings_server_use_defaults))
                }
                OutlinedButton(
                    onClick = { viewModel.actionChannel.trySend(ServerConfigAction.UseLocalhost) },
                    modifier = Modifier.weight(1f).testTag(ServerConfigTestTags.USE_LOCALHOST),
                ) {
                    Text(stringResource(Res.string.feature_settings_server_use_localhost))
                }
            }

            Button(
                onClick = { viewModel.actionChannel.trySend(ServerConfigAction.Submit) },
                modifier = Modifier.fillMaxWidth().testTag(ServerConfigTestTags.SAVE),
            ) {
                Text(stringResource(Res.string.feature_settings_server_save))
            }
        }
    }
}

@Composable
private fun Field(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    failure: ValidationFailure?,
    tag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    MifosOutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = label,
        keyboardType = keyboardType,
        isError = failure != null,
        errorText = failure?.let { stringResource(it.message()) },
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}

/**
 * Maps a typed failure to copy.
 *
 * This mapping is the whole point of the rewrite: it lives in the UI, where string resources belong,
 * instead of inside the rule. [ValidationFailure.TooShort] and [ValidationFailure.PortOutOfRange]
 * carry their own parameters, so copy that wants to name a bound can format it from the failure
 * rather than hardcoding a number — the mistake the login strings made.
 */
private fun ValidationFailure.message() = when (this) {
    ValidationFailure.Required -> Res.string.feature_settings_server_error_required
    is ValidationFailure.TooShort -> Res.string.feature_settings_server_error_too_short
    ValidationFailure.MalformedHostname -> Res.string.feature_settings_server_error_hostname
    is ValidationFailure.PortOutOfRange -> Res.string.feature_settings_server_error_port_range
    ValidationFailure.MalformedProtocol -> Res.string.feature_settings_server_error_protocol
    ValidationFailure.ApiPathMissingLeadingSlash -> Res.string.feature_settings_server_error_api_path_leading
    ValidationFailure.ApiPathMissingTrailingSlash -> Res.string.feature_settings_server_error_api_path_trailing
    ValidationFailure.MalformedTenant -> Res.string.feature_settings_server_error_tenant
}

/** Stable handles for the server-config screen — asserted by UI tests, never shown to a user. */
object ServerConfigTestTags {
    const val SCREEN = "server-config:screen"
    const val BACK = "server-config:back"
    const val PROTOCOL = "server-config:protocol"
    const val ENDPOINT = "server-config:endpoint"
    const val API_PATH = "server-config:api-path"
    const val PORT = "server-config:port"
    const val TENANT = "server-config:tenant"
    const val USE_DEFAULTS = "server-config:use-defaults"
    const val USE_LOCALHOST = "server-config:use-localhost"
    const val SAVE = "server-config:save"
}
