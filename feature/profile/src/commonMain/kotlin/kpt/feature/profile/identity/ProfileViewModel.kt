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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.data.auth.PasswordChangedSignInFailed
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.data.serverconfig.ServerConfigRepository
import kpt.core.data.session.SessionRepository
import kpt.core.data.staff.StaffRepository
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.users.User
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.getInstanceUrl
import kpt.core.model.utils.matching

/**
 * The signed-in officer's own account (screens/profile): who they are, which office Home counts,
 * which server they are on — and the one write, a password change.
 *
 * The session saved at sign-in (local, through `SessionRepository`) renders offline; `GET users/{id}`
 * (the SAME store and key Home's greeting reads) adds the name, email, roles and staff link. A failed
 * name read never blanks the screen: the session identity stays and only the name slot shows the error.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    sessions: SessionRepository,
    serverConfigs: ServerConfigRepository,
    dashboard: DashboardRepository,
    staff: StaffRepository,
    network: NetworkMonitor,
    private val auth: AuthCommandRepository,
) : BaseViewModel<ProfileUiState, ProfileEvent, ProfileAction>(ProfileUiState()) {

    /** The stored session; null until it first loads. Shared, so it is read once. */
    private val session: StateFlow<User?> = sessions.sessionStream(viewModelScope).state
        .mapNotNull { (it as? ScreenState.Content<User>)?.data }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    /** The user record's stream, keyed on the session's user — created once per user, as before. */
    private var userStream: ScreenDataStream<DashboardUser>? = null
    private val me: StateFlow<ScreenState<DashboardUser>> = session.filterNotNull()
        .map { it.userId }
        .distinctUntilChanged()
        .flatMapLatest { id -> dashboard.userStream(id, viewModelScope).also { userStream = it }.state }
        .stateIn(viewModelScope, SharingStarted.Lazily, ScreenState.Loading)

    /** The linked staff record, for the Loan officer / Staff label. Only read when there is a link. */
    private val staffRecord: Flow<Boolean?> = combine(me, session.filterNotNull()) { state, user ->
        (state as? ScreenState.Content<DashboardUser>)?.data?.staffId to user.officeId
    }.distinctUntilChanged().flatMapLatest { (staffId, officeId) ->
        if (staffId == null) {
            flowOf(null)
        } else {
            staff.staffStream(officeId.toInt(), viewModelScope).state.map { list ->
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
        combine(stateFlow, session, ::Pair),
        me,
        staffRecord,
        server,
        network.isOnline,
    ) { (ui, stored), me, isLoanOfficer, srv, online ->
        val session = stored ?: User()
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
            ProfileAction.Retry -> userStream?.retry()
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

/**
 * What the profile screen shows: the signed-in user, their staff link, the server session and the change-password
 * sheet.
 */
data class ProfileUiState(
    /** The signed-in username. */
    val username: String = "",
    /** Avatar initials, drawn the same way as on Home. */
    val initials: String = "",
    /** The user's office. */
    val officeName: String = "",
    /** Null until `users/{id}` answers, or when it never does. */
    val fullName: String? = null,
    /** The user's email address, when the user record has one. */
    val email: String? = null,
    /** The names of the user's Fineract roles. */
    val roles: List<String> = emptyList(),
    /** The display name of the linked staff record; null when there is no link. */
    val staffName: String? = null,
    /** True when the user is linked to a staff record. */
    val hasStaffLink: Boolean = false,
    /** Null when unknown (no link, or the staff read was refused) — the chip then says "Staff". */
    val isLoanOfficer: Boolean? = null,
    /** Whether the full name has loaded, is loading, or failed. */
    val name: NameState = NameState.Loading,
    /** False while the device has no network. */
    val online: Boolean = true,
    /** The server the app is signed in to. */
    val server: ServerSession = ServerSession(),
    /** Null = the change-password sheet is closed. */
    val password: PasswordForm? = null,
)

/** The load state of the user's full name. */
sealed interface NameState {
    /** The user record is still loading. */
    data object Loading : NameState

    /** The user record loaded. */
    data object Loaded : NameState

    /**
     * [reason] is the server's own message when it sent one.
     *
     * @property reason the server's message, when it sent one
     */
    data class Failed(val reason: String?) : NameState
}

/** The server session shown in the profile's server card. */
data class ServerSession(
    /** The instance's display name. */
    val instanceName: String = "",
    /** True when the config is one of the published instances — the "Live" badge. */
    val known: Boolean = false,
    /** The server's base URL. */
    val url: String = "",
    /** The Fineract tenant identifier. */
    val tenant: String = "",
)

/** The change-password sheet's form. */
data class PasswordForm(
    /** The new password as typed. */
    val new: String = "",
    /** The new password typed a second time. */
    val repeat: String = "",
    /** True while the change is being sent. */
    val submitting: Boolean = false,
    /** Why the last attempt failed; null when it has not failed. */
    val error: PasswordError? = null,
) {
    /** True once the repeat has text that differs from the new password. */
    val mismatch: Boolean get() = repeat.isNotEmpty() && repeat != new

    /** True when both fields are filled, they match, and nothing is being sent. */
    val canSave: Boolean get() = new.isNotEmpty() && repeat.isNotEmpty() && !mismatch && !submitting
}

/** Why a password change failed. */
sealed interface PasswordError {
    /**
     * [message] is the server's per-field messages, one per line; null → the screen's generic copy.
     *
     * @property message the server's per-field messages, one per line
     */
    data class Server(val message: String?) : PasswordError

    /** The device has no network; nothing was sent. */
    data object Offline : PasswordError
}

/** What the user did on the profile screen. */
sealed interface ProfileAction {
    /** Reload the user record after a failure. */
    data object Retry : ProfileAction

    /** Open the change-password sheet. */
    data object OpenChangePassword : ProfileAction

    /** Close the change-password sheet without saving. */
    data object DismissPassword : ProfileAction

    /**
     * The new-password field changed.
     *
     * @property value the new text
     */
    data class NewPassword(val value: String) : ProfileAction

    /**
     * The repeat-password field changed.
     *
     * @property value the new text
     */
    data class RepeatPassword(val value: String) : ProfileAction

    /** Send the password change. */
    data object SubmitPassword : ProfileAction
}

/** One-off effects of the profile screen, for the screen to act on. */
sealed interface ProfileEvent {
    /** The password was changed — the screen confirms and closes the sheet. */
    data object PasswordChanged : ProfileEvent
}
