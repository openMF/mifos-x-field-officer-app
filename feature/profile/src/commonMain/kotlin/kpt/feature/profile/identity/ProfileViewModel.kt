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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.data.auth.PasswordChangedSignInFailed
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.data.serverconfig.ServerConfigRepository
import kpt.core.data.staff.StaffRepository
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.getInstanceUrl
import kpt.core.model.utils.matching

/**
 * The signed-in officer's own account (screens/profile): who they are, which office Home counts,
 * which server they are on — and the one write, a password change.
 *
 * The session saved at sign-in renders at once and offline; `GET users/{id}` (the SAME store and key
 * Home's greeting reads) adds the name, email, roles and staff link. A failed name read never blanks
 * the screen: the session identity stays and only the name slot shows the error.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    project: ForkPreferencesRepository,
    serverConfigs: ServerConfigRepository,
    dashboard: DashboardRepository,
    staff: StaffRepository,
    network: NetworkMonitor,
    private val auth: AuthCommandRepository,
) : BaseViewModel<ProfileUiState, ProfileEvent, ProfileAction>(ProfileUiState()) {

    private val session = project.fineractUser.value
    private val userStream = dashboard.userStream(session.userId, viewModelScope)

    /** The linked staff record, for the Loan officer / Staff label. Only read when there is a link. */
    private val staffRecord: Flow<Boolean?> = userStream.state.flatMapLatest { state ->
        val staffId = (state as? ScreenState.Content<DashboardUser>)?.data?.staffId
        if (staffId == null) {
            flowOf(null)
        } else {
            staff.staffStream(session.officeId.toInt(), viewModelScope).state.map { list ->
                (list as? ScreenState.Content)?.data?.firstOrNull { it.id?.toLong() == staffId }?.isLoanOfficer
            }
        }
    }

    private val server: Flow<ServerSession> = combine(
        serverConfigs.serverConfigStream(viewModelScope).state,
        serverConfigs.instancesStream(viewModelScope).state,
    ) { config, instances ->
        val cfg = (config as? ScreenState.Content<ServerConfig>)?.data
        val instance = cfg?.let { c -> (instances as? ScreenState.Content)?.data?.matching(c) }
        ServerSession(
            instanceName = instance?.name ?: cfg?.endPoint.orEmpty(),
            known = instance != null,
            url = cfg?.getInstanceUrl().orEmpty(),
            tenant = cfg?.tenant.orEmpty(),
        )
    }

    /** What the screen renders. Derived — no `init {}` collector. */
    val profile: StateFlow<ProfileUiState> = combine(
        stateFlow,
        userStream.state,
        staffRecord,
        server,
        network.isOnline,
    ) { ui, me, isLoanOfficer, srv, online ->
        val self = (me as? ScreenState.Content<DashboardUser>)?.data
        ui.copy(
            username = session.username.orEmpty(),
            officeName = self?.officeName ?: session.officeName.orEmpty(),
            fullName = self?.let {
                listOfNotNull(it.firstname, it.lastname).filter(String::isNotBlank).joinToString(" ").ifBlank { null }
            },
            initials = initials(self, session.username.orEmpty()),
            email = self?.email,
            roles = self?.roles.orEmpty(),
            staffName = self?.staffName,
            hasStaffLink = self?.staffId != null,
            isLoanOfficer = isLoanOfficer,
            name = when (me) {
                is ScreenState.Content -> NameState.Loaded
                is ScreenState.Error -> NameState.Failed(me.error.serverErrorMessage())
                ScreenState.Loading -> NameState.Loading
                else -> NameState.Failed(null)
            },
            online = online,
            server = srv,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ProfileUiState())

    override fun handleAction(action: ProfileAction) {
        when (action) {
            ProfileAction.Retry -> userStream.retry()
            ProfileAction.OpenChangePassword -> updateState { copy(password = PasswordForm()) }
            ProfileAction.DismissPassword -> updateState { copy(password = null) }
            is ProfileAction.NewPassword -> updatePassword { copy(new = action.value, error = null) }
            is ProfileAction.RepeatPassword -> updatePassword { copy(repeat = action.value, error = null) }
            ProfileAction.SubmitPassword -> submitPassword()
        }
    }

    private fun submitPassword() {
        val form = state.password ?: return
        if (!form.canSave) return
        updatePassword { copy(submitting = true, error = null) }
        viewModelScope.launch {
            when (val result = auth.changePassword(form.new, form.repeat)) {
                is MutationResult.Applied -> {
                    updateState { copy(password = null) }
                    sendEvent(ProfileEvent.PasswordChanged)
                }
                is MutationResult.Failed -> if (result.cause is PasswordChangedSignInFailed) {
                    // Signed out (data kept) — the root graph is already moving to sign-in.
                    updateState { copy(password = null) }
                } else {
                    updatePassword {
                        copy(submitting = false, error = PasswordError.Server(result.cause.serverErrorMessage()))
                    }
                }
                is MutationResult.Blocked -> updatePassword {
                    val error = if (result.reason == BlockReason.OFFLINE) {
                        PasswordError.Offline
                    } else {
                        PasswordError.Server(null)
                    }
                    copy(submitting = false, error = error)
                }
                is MutationResult.Conflicted ->
                    updatePassword { copy(submitting = false, error = PasswordError.Server(null)) }
            }
        }
    }

    private fun updatePassword(transform: PasswordForm.() -> PasswordForm) =
        updateState { copy(password = password?.transform()) }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L

        /** First + last initials, as the Home avatar draws them; else the username's first letter. */
        fun initials(self: DashboardUser?, username: String): String =
            listOfNotNull(self?.firstname, self?.lastname).filter(String::isNotBlank)
                .joinToString("") { it.first().uppercase() }
                .ifEmpty { username.take(1).uppercase() }
    }
}

data class ProfileUiState(
    val username: String = "",
    val initials: String = "",
    val officeName: String = "",
    /** Null until `users/{id}` answers, or when it never does. */
    val fullName: String? = null,
    val email: String? = null,
    val roles: List<String> = emptyList(),
    val staffName: String? = null,
    val hasStaffLink: Boolean = false,
    /** Null when unknown (no link, or the staff read was refused) — the chip then says "Staff". */
    val isLoanOfficer: Boolean? = null,
    val name: NameState = NameState.Loading,
    val online: Boolean = true,
    val server: ServerSession = ServerSession(),
    /** Null = the change-password sheet is closed. */
    val password: PasswordForm? = null,
)

sealed interface NameState {
    data object Loading : NameState
    data object Loaded : NameState

    /** [reason] is the server's own message when it sent one. */
    data class Failed(val reason: String?) : NameState
}

data class ServerSession(
    val instanceName: String = "",
    /** True when the config is one of the published instances — the "Live" badge. */
    val known: Boolean = false,
    val url: String = "",
    val tenant: String = "",
)

data class PasswordForm(
    val new: String = "",
    val repeat: String = "",
    val submitting: Boolean = false,
    val error: PasswordError? = null,
) {
    val mismatch: Boolean get() = repeat.isNotEmpty() && repeat != new
    val canSave: Boolean get() = new.isNotEmpty() && repeat.isNotEmpty() && !mismatch && !submitting
}

sealed interface PasswordError {
    /** [message] is the server's per-field messages, one per line; null → the screen's generic copy. */
    data class Server(val message: String?) : PasswordError
    data object Offline : PasswordError
}

sealed interface ProfileAction {
    data object Retry : ProfileAction
    data object OpenChangePassword : ProfileAction
    data object DismissPassword : ProfileAction
    data class NewPassword(val value: String) : ProfileAction
    data class RepeatPassword(val value: String) : ProfileAction
    data object SubmitPassword : ProfileAction
}

sealed interface ProfileEvent {
    data object PasswordChanged : ProfileEvent
}
