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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.datastore.prefs.ProjectPreferencesRepository
import kpt.core.domain.validation.ServerConfigRules
import kpt.core.domain.validation.ValidationFailure
import kpt.core.domain.validation.ValidationResult
import kpt.core.model.utils.ServerConfig

/**
 * Editor for the Fineract server configuration.
 *
 * Ported from `6b66e8a43:feature/settings/.../updateServer/UpdateServerConfigViewModel.kt`. Reached
 * from the sign-in screen, because a first-run user has no server URL and therefore cannot sign in
 * until this exists — which is why S1 restores it alongside login rather than leaving it to S7.
 *
 * Two changes from the original, both consequences of the validator rewrite (spec §2):
 *
 *  - The original exposed five `StateFlow<String?>` error streams, one per field, each built from
 *    `snapshotFlow { … }.mapLatest { validator.validateX(it).message }`. The message came
 *    pre-localized out of the domain layer, which is what forced `getString` into `core/domain`.
 *    Here validation is a pure function over state and the failures are TYPED — the screen maps them
 *    to strings, so no Compose resource is touched below the UI.
 *  - Validation runs on SUBMIT as well as per keystroke. The original validated only reactively, so
 *    nothing stopped `UpdateServerConfig` being dispatched while a field was invalid; it wrote
 *    whatever was in the fields.
 */
class ServerConfigViewModel(
    private val preferences: ProjectPreferencesRepository,
) : BaseViewModel<ServerConfigState, ServerConfigEvent, ServerConfigAction>(
    ServerConfigState(config = preferences.serverConfig.value),
) {

    override fun handleAction(action: ServerConfigAction) {
        when (action) {
            is ServerConfigAction.ProtocolChanged -> edit { copy(protocol = action.value) }
            is ServerConfigAction.EndPointChanged -> edit { copy(endPoint = action.value) }
            is ServerConfigAction.ApiPathChanged -> edit { copy(apiPath = action.value) }
            is ServerConfigAction.PortChanged -> edit { copy(port = action.value) }
            is ServerConfigAction.TenantChanged -> edit { copy(tenant = action.value) }
            ServerConfigAction.UseDefaults -> replace(ServerConfig.DEFAULT)
            ServerConfigAction.UseLocalhost -> replace(ServerConfig.LOCALHOST)
            ServerConfigAction.Submit -> submit()
        }
    }

    private fun edit(mutate: ServerConfig.() -> ServerConfig) {
        val next = state.config.mutate()
        mutableStateFlow.value = state.copy(config = next, failures = validate(next))
    }

    private fun replace(config: ServerConfig) {
        mutableStateFlow.value = state.copy(config = config, failures = validate(config))
    }

    private fun submit() {
        val failures = validate(state.config)
        if (failures.isNotEmpty()) {
            // Re-publish so a user who never touched a field still sees why submit refused.
            mutableStateFlow.value = state.copy(failures = failures)
            sendEvent(ServerConfigEvent.Invalid)
            return
        }
        viewModelScope.launch {
            preferences.updateServerConfig(state.config)
            sendEvent(ServerConfigEvent.Saved)
        }
    }

    /**
     * Every field's failure, keyed by field.
     *
     * A map rather than five properties: the screen needs per-field attribution, and submit needs to
     * know whether ANY field is invalid. Both read the same computation, so the two cannot disagree —
     * the original computed them in five independent flows and had no combined view at all.
     */
    private fun validate(config: ServerConfig): Map<ServerConfigField, ValidationFailure> = buildMap {
        putFailure(ServerConfigField.Protocol, ServerConfigRules.protocol(config.protocol))
        putFailure(ServerConfigField.EndPoint, ServerConfigRules.endpoint(config.endPoint))
        putFailure(ServerConfigField.ApiPath, ServerConfigRules.apiPath(config.apiPath))
        putFailure(ServerConfigField.Port, ServerConfigRules.port(config.port))
        putFailure(ServerConfigField.Tenant, ServerConfigRules.tenant(config.tenant))
    }

    private fun MutableMap<ServerConfigField, ValidationFailure>.putFailure(
        field: ServerConfigField,
        result: ValidationResult,
    ) {
        if (result is ValidationResult.Invalid) put(field, result.failure)
    }
}

/** The five editable fields, so failures can be attributed to one. */
enum class ServerConfigField { Protocol, EndPoint, ApiPath, Port, Tenant }

data class ServerConfigState(
    val config: ServerConfig,
    val failures: Map<ServerConfigField, ValidationFailure> = emptyMap(),
) {
    val isValid: Boolean get() = failures.isEmpty()
}

sealed interface ServerConfigAction {
    data class ProtocolChanged(val value: String) : ServerConfigAction
    data class EndPointChanged(val value: String) : ServerConfigAction
    data class ApiPathChanged(val value: String) : ServerConfigAction
    data class PortChanged(val value: String) : ServerConfigAction
    data class TenantChanged(val value: String) : ServerConfigAction

    /** Restores `ServerConfig.DEFAULT` — the original's `UseDefaultConfig`. */
    data object UseDefaults : ServerConfigAction

    /** Not in the original; `ServerConfig.LOCALHOST` already exists in the substrate. */
    data object UseLocalhost : ServerConfigAction

    data object Submit : ServerConfigAction
}

sealed interface ServerConfigEvent {
    data object Saved : ServerConfigEvent
    data object Invalid : ServerConfigEvent
}
