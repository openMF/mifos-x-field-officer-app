/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.serverconfig

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.data.serverconfig.ServerConfigRepository
import kpt.core.data.session.SessionRepository
import kpt.core.domain.validation.ServerConfigRules
import kpt.core.domain.validation.ValidationFailure
import kpt.core.domain.validation.ValidationResult
import kpt.core.model.objects.users.User
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.ServerInstance
import kpt.core.model.utils.matching

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
 *
 * The stored config is read through `ServerConfigRepository.serverConfigStream` — the same
 * store-backed read path as every other screen — and written through its `updateServerConfig`,
 * never by reaching into `core/datastore`.
 *
 * **What the form shows is derived, not seeded** (no `init {}` collector): [form] is the officer's
 * [ServerConfigState.draft] when they have edited a field, else the STORED config. A draft is never
 * overwritten by a re-emission of the preference, and nothing can submit a placeholder: before the
 * stored config arrives there is no draft and nothing to submit.
 *
 * **Saving a DIFFERENT server signs the officer out** — as the original did (`prefManager.logOut()`
 * before `updateServerConfig`): a session belongs to the server that issued it. The original then asked
 * for an app restart, because its server was read once at start-up; requests now follow the saved
 * config immediately (`ServerConfigBaseUrlOverride`), so no restart. Reached before sign-in (from the
 * sign-in screen) there is no session and the sign-out is a no-op. Local data is kept, as the original
 * kept it — unsynced work must not be lost to a server change.
 */
class ServerConfigViewModel(
    private val repository: ServerConfigRepository,
    private val auth: AuthCommandRepository,
    session: SessionRepository,
) : BaseViewModel<ServerConfigState, ServerConfigEvent, ServerConfigAction>(ServerConfigState()) {

    /** The saved config. Eager: a submit must compare against it even with no screen collecting. */
    private val stored: StateFlow<ServerConfig?> = repository.serverConfigStream(viewModelScope).state
        .map { (it as? ScreenState.Content<ServerConfig>)?.data }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Whether an officer is signed in — the same screen serves sign-in (nobody yet: save directly) and
     * Settings (a session on the OLD server: changing it signs out, so confirm first). Eager, like
     * [stored]: submit reads it with no screen collecting.
     */
    private val signedInUser: StateFlow<User?> = session.sessionStream(viewModelScope).state
        .map { state -> (state as? ScreenState.Content)?.data?.takeIf { it.isAuthenticated } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val signedIn: Boolean get() = signedInUser.value != null

    /** The instance list read — retained so [ServerConfigAction.RetryInstances] re-runs it. */
    private val instancesStream = repository.instancesStream(viewModelScope)

    /**
     * The picker's own state — Loading (progress) until the first list lands, then Content; Error /
     * NoNetwork with nothing cached. The five fields stay usable throughout: a custom server needs no list.
     */
    val instances: StateFlow<ScreenState<List<ServerInstance>>> =
        instancesStream.state.stateIn(viewModelScope, SharingStarted.Eagerly, ScreenState.Loading)

    /** What the screen renders: the draft over the stored config, with the instances to pick from. */
    val form: StateFlow<ServerConfigState> = combine(
        stateFlow,
        stored,
        instances,
        signedInUser,
    ) { ui, saved, list, user ->
        ui.copy(
            config = ui.draft ?: saved ?: repository.defaultConfig,
            instances = (list as? ScreenState.Content<List<ServerInstance>>)?.data.orEmpty(),
            // The server this session runs on — the SAVED config, never the draft being edited.
            live = saved?.takeIf { user != null },
            liveUsername = user?.username,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ServerConfigState(config = repository.defaultConfig))

    override fun handleAction(action: ServerConfigAction) {
        when (action) {
            is ServerConfigAction.ProtocolChanged -> edit { copy(protocol = action.value) }
            is ServerConfigAction.EndPointChanged -> edit { copy(endPoint = action.value) }
            is ServerConfigAction.ApiPathChanged -> edit { copy(apiPath = action.value) }
            is ServerConfigAction.PortChanged -> edit { copy(port = action.value) }
            is ServerConfigAction.TenantChanged -> edit { copy(tenant = action.value) }
            is ServerConfigAction.InstanceSelected ->
                form.value.instances.firstOrNull { it.id == action.id }?.let { replace(it.config) }
            ServerConfigAction.Submit -> submit(confirmed = false)
            ServerConfigAction.SwitchConfirmed -> submit(confirmed = true)
            ServerConfigAction.SwitchCancelled -> mutableStateFlow.value = state.copy(confirmingSwitch = false)
            ServerConfigAction.RetryInstances -> instancesStream.retry()
        }
    }

    private fun edit(mutate: ServerConfig.() -> ServerConfig) = replace(form.value.config.mutate())

    private fun replace(config: ServerConfig) {
        mutableStateFlow.value = state.copy(draft = config, failures = validate(config))
    }

    private fun submit(confirmed: Boolean) {
        mutableStateFlow.value = state.copy(confirmingSwitch = false)
        val saved = stored.value
        // Nothing loaded and nothing typed: the fields hold a placeholder, not the user's config.
        val config = state.draft ?: saved
        if (config != null && isValid(config)) {
            val switching = saved != null && saved != config
            // A signed-in officer changing server loses the session (it belongs to the old server) —
            // ask first. Signed out (from sign-in) there is nothing to lose: save directly.
            if (switching && signedIn && !confirmed) {
                mutableStateFlow.value = state.copy(confirmingSwitch = true)
            } else {
                save(config, switching)
            }
        }
    }

    private fun isValid(config: ServerConfig): Boolean {
        val failures = validate(config)
        if (failures.isNotEmpty()) {
            // Re-publish so a user who never touched a field still sees why submit refused.
            mutableStateFlow.value = state.copy(failures = failures)
            sendEvent(ServerConfigEvent.Invalid)
        }
        return failures.isEmpty()
    }

    private fun save(config: ServerConfig, switching: Boolean) {
        viewModelScope.launch {
            when (repository.updateServerConfig(config)) {
                is MutationResult.Failed -> sendEvent(ServerConfigEvent.SaveFailed)
                else -> {
                    if (switching) auth.signOut(clearLocalData = false)
                    sendEvent(ServerConfigEvent.Saved)
                }
            }
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
    /** What the fields show — set by [ServerConfigViewModel.form]; read it from there. */
    val config: ServerConfig = ServerConfig.LOCALHOST,
    /** The officer's edits; null until a field is changed. */
    val draft: ServerConfig? = null,
    val failures: Map<ServerConfigField, ValidationFailure> = emptyMap(),
    /** The named servers to pick from — set by [ServerConfigViewModel.form]. */
    val instances: List<ServerInstance> = emptyList(),
    /** A signed-in officer asked to save a different server; the screen confirms the sign-out. */
    val confirmingSwitch: Boolean = false,
    /**
     * The server the current session is signed in to; null when nobody is signed in (sign-in's copy of this
     * screen).
     */
    val live: ServerConfig? = null,
    val liveUsername: String? = null,
) {
    /** The named instance the session runs on, or null for a custom server. */
    val liveInstance: ServerInstance? get() = live?.let { instances.matching(it) }

    val isValid: Boolean get() = failures.isEmpty()

    /** The instance the fields match exactly, or null when the officer typed a custom server. */
    val selectedInstance: ServerInstance? get() = instances.matching(config)
}

sealed interface ServerConfigAction {
    data class ProtocolChanged(val value: String) : ServerConfigAction
    data class EndPointChanged(val value: String) : ServerConfigAction
    data class ApiPathChanged(val value: String) : ServerConfigAction
    data class PortChanged(val value: String) : ServerConfigAction
    data class TenantChanged(val value: String) : ServerConfigAction

    /**
     * Fill every field from a named instance. Replaces the original's "Use default config" and the
     * port's "Use localhost" buttons — both are now instances, beside the demo servers.
     */
    data class InstanceSelected(val id: String) : ServerConfigAction

    data object Submit : ServerConfigAction

    /** The officer confirmed signing out to switch server. */
    data object SwitchConfirmed : ServerConfigAction

    data object SwitchCancelled : ServerConfigAction

    /** Re-fetch the instance list after a failed load. */
    data object RetryInstances : ServerConfigAction
}

sealed interface ServerConfigEvent {
    data object Saved : ServerConfigEvent
    data object Invalid : ServerConfigEvent

    /** The write itself failed. Reported, not swallowed: the screen must not claim it was saved. */
    data object SaveFailed : ServerConfigEvent
}
