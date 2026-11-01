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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.icon.AppIcons
import kpt.core.domain.validation.ValidationFailure
import kpt.core.model.utils.DemoCredentials
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.ServerInstance
import kpt.core.model.utils.baseUrl
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_server_api_path
import kpt.feature.settings.generated.resources.feature_settings_server_config_title
import kpt.feature.settings.generated.resources.feature_settings_server_demo_account
import kpt.feature.settings.generated.resources.feature_settings_server_demo_badge
import kpt.feature.settings.generated.resources.feature_settings_server_demo_credentials
import kpt.feature.settings.generated.resources.feature_settings_server_details_title
import kpt.feature.settings.generated.resources.feature_settings_server_endpoint
import kpt.feature.settings.generated.resources.feature_settings_server_error_api_path_leading
import kpt.feature.settings.generated.resources.feature_settings_server_error_api_path_trailing
import kpt.feature.settings.generated.resources.feature_settings_server_error_hostname
import kpt.feature.settings.generated.resources.feature_settings_server_error_port_range
import kpt.feature.settings.generated.resources.feature_settings_server_error_protocol
import kpt.feature.settings.generated.resources.feature_settings_server_error_required
import kpt.feature.settings.generated.resources.feature_settings_server_error_tenant
import kpt.feature.settings.generated.resources.feature_settings_server_error_too_short
import kpt.feature.settings.generated.resources.feature_settings_server_instance_custom
import kpt.feature.settings.generated.resources.feature_settings_server_instances_title
import kpt.feature.settings.generated.resources.feature_settings_server_invalid
import kpt.feature.settings.generated.resources.feature_settings_server_live_badge
import kpt.feature.settings.generated.resources.feature_settings_server_live_tenant
import kpt.feature.settings.generated.resources.feature_settings_server_live_title
import kpt.feature.settings.generated.resources.feature_settings_server_live_user
import kpt.feature.settings.generated.resources.feature_settings_server_port
import kpt.feature.settings.generated.resources.feature_settings_server_protocol
import kpt.feature.settings.generated.resources.feature_settings_server_save
import kpt.feature.settings.generated.resources.feature_settings_server_save_failed
import kpt.feature.settings.generated.resources.feature_settings_server_saved
import kpt.feature.settings.generated.resources.feature_settings_server_switch_cancel
import kpt.feature.settings.generated.resources.feature_settings_server_switch_confirm
import kpt.feature.settings.generated.resources.feature_settings_server_switch_message
import kpt.feature.settings.generated.resources.feature_settings_server_switch_title
import kpt.feature.settings.generated.resources.feature_settings_server_tenant
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
    val state by viewModel.form.collectAsStateWithLifecycle()
    val instances by viewModel.instances.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(Res.string.feature_settings_server_saved)
    val invalidMessage = stringResource(Res.string.feature_settings_server_invalid)
    val saveFailedMessage = stringResource(Res.string.feature_settings_server_save_failed)

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
                ServerConfigEvent.SaveFailed -> snackbarHostState.showSnackbar(saveFailedMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Pinned, not at the end of the scroll: picking an instance is one tap, and Save must not be
        // a scroll away from it on a phone where the five fields push it off-screen.
        bottomBar = {
            Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
                Button(
                    onClick = { viewModel.actionChannel.trySend(ServerConfigAction.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag(ServerConfigTestTags.SAVE),
                ) {
                    Text(stringResource(Res.string.feature_settings_server_save))
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_settings_server_config_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag(ServerConfigTestTags.BACK)) {
                        Icon(AppIcons.ArrowBack, contentDescription = null)
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
            // Which server this session is on — read from the SAVED config, so editing the fields
            // below never changes it until a switch is confirmed. Absent on sign-in (no session yet).
            state.live?.let { LiveServerCard(it, state.liveInstance, state.liveUsername) }

            Text(
                text = stringResource(Res.string.feature_settings_server_instances_title),
                style = MaterialTheme.typography.titleSmall,
            )
            // The list loads through its store: progress until it lands, the framework's error /
            // no-network state with a retry when nothing is cached. The fields below never wait on it.
            ScreenContent(
                state = instances,
                onRetry = { viewModel.actionChannel.trySend(ServerConfigAction.RetryInstances) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp).testTag(ServerConfigTestTags.INSTANCES),
            ) { _, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InstancePicker(
                        state = state,
                        onSelect = { viewModel.actionChannel.trySend(ServerConfigAction.InstanceSelected(it)) },
                    )
                    state.selectedInstance?.takeIf { it.isDemo }?.let { DemoCredentialsHint(it.demoAccounts) }
                }
            }

            Text(
                text = stringResource(Res.string.feature_settings_server_details_title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
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
        }
    }

    if (state.confirmingSwitch) {
        AlertDialog(
            onDismissRequest = { viewModel.actionChannel.trySend(ServerConfigAction.SwitchCancelled) },
            title = { Text(stringResource(Res.string.feature_settings_server_switch_title)) },
            text = {
                Text(
                    stringResource(
                        Res.string.feature_settings_server_switch_message,
                        state.selectedInstance?.name ?: state.config.endPoint,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.actionChannel.trySend(ServerConfigAction.SwitchConfirmed) },
                    modifier = Modifier.testTag(ServerConfigTestTags.SWITCH_CONFIRM),
                ) { Text(stringResource(Res.string.feature_settings_server_switch_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.actionChannel.trySend(ServerConfigAction.SwitchCancelled) }) {
                    Text(stringResource(Res.string.feature_settings_server_switch_cancel))
                }
            },
        )
    }
}

/**
 * The named servers as one-tap choices — selecting one fills all five fields. "Custom" is not a
 * choice: it is what the list shows when the fields match no instance (the officer typed their own).
 */
@Composable
private fun InstancePicker(state: ServerConfigState, onSelect: (String) -> Unit) {
    val selected = state.selectedInstance
    Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.instances.forEach { instance ->
            InstanceRow(
                name = instance.name,
                detail = "${instance.config.endPoint} · ${instance.config.tenant}",
                isDemo = instance.isDemo,
                isLive = instance.id == state.liveInstance?.id,
                selected = instance.id == selected?.id,
                onClick = { onSelect(instance.id) },
                modifier = Modifier.testTag(ServerConfigTestTags.INSTANCE_PREFIX + instance.id),
            )
        }
        if (selected == null) {
            InstanceRow(
                name = stringResource(Res.string.feature_settings_server_instance_custom),
                detail = "${state.config.endPoint} · ${state.config.tenant}",
                isDemo = false,
                isLive = state.live != null && state.liveInstance == null && state.live == state.config,
                selected = true,
                // intentional-noop: "Custom" is a status, not a choice — edit the fields below to change it.
                onClick = {},
            )
        }
    }
}

@Composable
private fun InstanceRow(
    name: String,
    detail: String,
    isDemo: Boolean,
    isLive: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RadioButton(selected = selected, onClick = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isLive) {
                Badge(
                    text = stringResource(Res.string.feature_settings_server_live_badge),
                    container = MaterialTheme.colorScheme.primaryContainer,
                    content = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.testTag(ServerConfigTestTags.LIVE_BADGE),
                )
            }
            if (isDemo) {
                Badge(
                    text = stringResource(Res.string.feature_settings_server_demo_badge),
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

/** The selected demo instance's PUBLISHED sign-ins, one line per role — so a tester knows what to type. */
@Composable
private fun DemoCredentialsHint(accounts: List<DemoCredentials>) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth().testTag(ServerConfigTestTags.DEMO_HINT),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(AppIcons.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(Res.string.feature_settings_server_demo_credentials),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                accounts.forEach {
                    Text(
                        text = stringResource(
                            Res.string.feature_settings_server_demo_account,
                            it.role,
                            it.username,
                            it.password,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
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
    AppOutlinedTextField(
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
    const val LIVE = "server-config:live"
    const val LIVE_BADGE = "server-config:live-badge"
    const val BACK = "server-config:back"
    const val PROTOCOL = "server-config:protocol"
    const val ENDPOINT = "server-config:endpoint"
    const val API_PATH = "server-config:api-path"
    const val PORT = "server-config:port"
    const val TENANT = "server-config:tenant"
    const val INSTANCE_PREFIX = "server-config:instance:"
    const val DEMO_HINT = "server-config:demo-hint"
    const val INSTANCES = "server-config:instances"
    const val SWITCH_CONFIRM = "server-config:switch-confirm"
    const val SAVE = "server-config:save"
}

@Composable
private fun Badge(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Surface(shape = MaterialTheme.shapes.small, color = container, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = content,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

/** "Signed in to …": the session's server, its address and tenant, and who is signed in. */
@Composable
private fun LiveServerCard(live: ServerConfig, instance: ServerInstance?, username: String?) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth().testTag(ServerConfigTestTags.LIVE),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(Res.string.feature_settings_server_live_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = instance?.name ?: stringResource(Res.string.feature_settings_server_instance_custom),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = live.baseUrl(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(Res.string.feature_settings_server_live_tenant, live.tenant),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            username?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(Res.string.feature_settings_server_live_user, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
