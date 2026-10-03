/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.datastore.prefs.ProjectPreferencesRepository
import kpt.core.domain.validation.CredentialRules
import kpt.core.domain.validation.ValidationResult
import kpt.core.model.objects.users.User
import kpt.core.network.mifos.auth.dto.PostAuthenticationRequest
import kpt.core.network.mifos.auth.dto.PostAuthenticationResponse

/**
 * Sign-in against the configured Fineract instance.
 *
 * Authentication is the one write that CANNOT be optimistic — there is no local credential store to
 * check against, so [AuthCommandRepository] runs it `OnlineRequired` and an offline attempt comes
 * back [MutationResult.Blocked]. That is surfaced as its own message rather than a generic failure:
 * "wrong password" and "no signal" are different problems and a field officer acts on them
 * differently.
 */
class SignInViewModel(
    private val auth: AuthCommandRepository,
    private val preferences: ProjectPreferencesRepository,
) : BaseViewModel<SignInState, SignInEvent, SignInAction>(SignInState()) {

    override fun handleAction(action: SignInAction) {
        when (action) {
            is SignInAction.UsernameChanged ->
                mutableStateFlow.value = state.copy(username = action.value, error = null)

            is SignInAction.PasswordChanged ->
                mutableStateFlow.value = state.copy(password = action.value, error = null)

            SignInAction.Submit -> submit()
        }
    }

    private fun submit() {
        val username = state.username.trim()
        val password = state.password
        // Validated here rather than on the server: a round trip to be told the field is too short
        // is a round trip a field officer often cannot afford.
        val validation = validate(username, password)
        if (validation != null) {
            mutableStateFlow.value = state.copy(error = validation)
            return
        }
        mutableStateFlow.value = state.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            when (val result = auth.authenticate(PostAuthenticationRequest(username = username, password = password))) {
                is MutationResult.Applied -> onAuthenticated(result.value, username, password)
                is MutationResult.Blocked ->
                    fail(if (result.reason == BlockReason.OFFLINE) SignInError.Offline else SignInError.Rejected)
                is MutationResult.Failed -> {
                    // The cause was being DISCARDED. A sign-in that fails for a transport reason —
                    // a wrong base path, a serialization mismatch, a TLS error — looks identical on
                    // screen to a wrong password, and with nothing in the log there is no way to
                    // tell them apart from a device. Diagnosing the 2026-10-01 "Login failed"
                    // (a 404 from a wrong `base_path`) took a packet-level probe for exactly this
                    // reason. The message the user sees is unchanged.
                    Logger.e(tag = TAG, throwable = result.cause) { "authenticate failed" }
                    fail(SignInError.Rejected)
                }
                is MutationResult.Conflicted -> fail(SignInError.Rejected)
            }
        }
    }

    /**
     * Fineract answers 200 with `authenticated = false` for a bad credential rather than a 4xx, so
     * a successful CALL is not a successful LOGIN — the flag has to be checked explicitly.
     */
    private suspend fun onAuthenticated(
        response: PostAuthenticationResponse,
        username: String,
        password: String,
    ) {
        if (response.authenticated != true) {
            fail(SignInError.Rejected)
            return
        }
        preferences.updateFineractUser(
            User(
                username = username,
                // password DELIBERATELY NOT PERSISTED.
                //
                // The original wrote the raw password into preferences and this port carried it
                // over; S1's ledger flagged it as requiring a written justification or removal and
                // it was never closed. Measured 2026-10-02: NOTHING reads `User.password` —
                // every consumer of `fineractUser` reads `username` or
                // `base64EncodedAuthenticationKey`, and `authHeader` derives from the encoded key.
                // Fineract's subsequent Basic auth needs that key, not the password, so storing
                // the password bought nothing and kept a credential on disk for the life of the
                // session. Locked by `theRawPasswordIsNeverPersisted`.
                userId = response.userId ?: 0,
                base64EncodedAuthenticationKey = response.base64EncodedAuthenticationKey,
                isAuthenticated = true,
                officeId = response.officeId ?: 0,
                officeName = response.officeName,
                permissions = response.permissions.orEmpty(),
            ),
        )
        mutableStateFlow.value = state.copy(isSubmitting = false)
        sendEvent(SignInEvent.LoggedIn)
    }

    private fun fail(error: SignInError) {
        mutableStateFlow.value = state.copy(isSubmitting = false, error = error)
    }

    /**
     * Credential rules come from [CredentialRules], never from a local constant.
     *
     * This ViewModel previously declared its own `MIN_USERNAME = 5` while the original app's
     * `UsernameValidationUseCase` rejected `length < 4` (`897ffdac1`). Nothing related the two, so a
     * four-character username that signed in on the original was refused here — a regression
     * introduced by re-authoring the screen rather than porting it, and then baked into the
     * user-facing copy ("at least 5 characters"). Reading through the shared rules is what makes the
     * thresholds impossible to diverge again.
     */
    private companion object {
        const val TAG = "SignInViewModel"
    }

    private fun validate(username: String, password: String): SignInError? = when {
        CredentialRules.username(username) is ValidationResult.Invalid -> SignInError.UsernameTooShort
        CredentialRules.password(password) is ValidationResult.Invalid -> SignInError.PasswordTooShort
        else -> null
    }
}

data class SignInState(
    val username: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val error: SignInError? = null,
)

/** Distinct causes, because the officer's next step differs for each. */
enum class SignInError { UsernameTooShort, PasswordTooShort, Rejected, Offline }

sealed interface SignInAction {
    data class UsernameChanged(val value: String) : SignInAction
    data class PasswordChanged(val value: String) : SignInAction
    data object Submit : SignInAction
}

sealed interface SignInEvent {
    data object LoggedIn : SignInEvent
}
