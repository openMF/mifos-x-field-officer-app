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

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.datastore.prefs.ProjectPreferencesRepository
import kpt.core.datastore.prefs.ProjectPreferencesRepositoryImpl
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.domain.validation.CredentialRules
import kpt.core.network.mifos.auth.dto.PostAuthenticationRequest
import kpt.core.network.mifos.auth.dto.PostAuthenticationResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    /** Records whether the server was reached, so local validation can be proven to short-circuit. */
    private class FakeAuth(private val result: MutationResult<PostAuthenticationResponse>) : AuthCommandRepository {
        var calls = 0
        override suspend fun authenticate(
            postAuthenticationRequest: PostAuthenticationRequest,
        ): MutationResult<PostAuthenticationResponse> {
            calls++
            return result
        }
    }

    private val dispatchers = object : DispatcherManager {
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: MainCoroutineDispatcher = Dispatchers.Main
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        override val appScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }

    /**
     * The REAL preferences seam over in-memory settings rather than a hand-written fake: the
     * interface inherits 22 framework members, so a fake would be 29 stubs that prove nothing, and
     * this additionally exercises the persistence the ViewModel depends on.
     */
    private fun preferences(): ProjectPreferencesRepository {
        val plain = MapSettings()
        val secure = MapSettings()
        return ProjectPreferencesRepositoryImpl(
            delegate = UserPreferencesRepositoryImpl(plain, secure, dispatchers),
            plainSettings = plain,
            secureSettings = secure,
            dispatcher = dispatchers,
        )
    }

    private fun response(authenticated: Boolean) = PostAuthenticationResponse(
        authenticated = authenticated,
        base64EncodedAuthenticationKey = "a2V5",
        userId = 7,
        officeId = 3,
        officeName = "Head Office",
        permissions = listOf("ALL_FUNCTIONS"),
    )

    private fun submitValid(model: SignInViewModel) {
        model.actionChannel.trySend(SignInAction.UsernameChanged("officer"))
        model.actionChannel.trySend(SignInAction.PasswordChanged("password1"))
        model.actionChannel.trySend(SignInAction.Submit)
    }

    @Test
    fun shortUsernameIsRejectedWithoutCallingTheServer() = runTest(dispatcher) {
        val auth = FakeAuth(MutationResult.Applied(response(true), synced = true))
        val model = SignInViewModel(auth, preferences())
        model.actionChannel.trySend(SignInAction.UsernameChanged("abc"))
        model.actionChannel.trySend(SignInAction.PasswordChanged("longenough"))
        model.actionChannel.trySend(SignInAction.Submit)
        runCurrent()
        assertEquals(SignInError.UsernameTooShort, model.stateFlow.value.error)
        // Validating locally exists to save a round trip the officer may not be able to make.
        assertEquals(0, auth.calls)
    }

    @Test
    fun offlineIsReportedAsOfflineNotAsBadCredentials() = runTest(dispatcher) {
        val model = SignInViewModel(FakeAuth(MutationResult.Blocked(BlockReason.OFFLINE)), preferences())
        submitValid(model)
        runCurrent()
        // Different problems: one is solved by finding signal, the other by retyping a password.
        assertEquals(SignInError.Offline, model.stateFlow.value.error)
    }

    @Test
    fun authenticatedFalseIsAFailedLoginEvenThoughTheCallSucceeded() = runTest(dispatcher) {
        // Fineract answers 200 with authenticated=false for a bad credential rather than a 4xx,
        // so a successful CALL is not a successful LOGIN.
        val model = SignInViewModel(FakeAuth(MutationResult.Applied(response(false), synced = true)), preferences())
        submitValid(model)
        runCurrent()
        assertEquals(SignInError.Rejected, model.stateFlow.value.error)
    }

    @Test
    fun successPersistsTheFineractUserAndSignalsTheShell() = runTest(dispatcher) {
        val prefs = preferences()
        val model = SignInViewModel(FakeAuth(MutationResult.Applied(response(true), synced = true)), prefs)
        var loggedIn = false
        val job = launch { model.eventFlow.collect { if (it is SignInEvent.LoggedIn) loggedIn = true } }
        submitValid(model)
        runCurrent()
        assertNull(model.stateFlow.value.error)
        assertEquals("officer", prefs.fineractUser.value.username)
        assertEquals("a2V5", prefs.fineractUser.value.base64EncodedAuthenticationKey)
        // The raw password is NOT kept. Fineract's subsequent Basic auth uses the encoded key, and
        // nothing in the app reads `User.password` — so persisting it only left a credential on
        // disk for the life of the session. Flagged OPEN in S1's ledger, closed 2026-10-02.
        assertNull(prefs.fineractUser.value.password)
        assertTrue(prefs.fineractUser.value.isAuthenticated)
        // The token the network layer reads must not lag the user record — and it is the RAW
        // credential, not a pre-formatted header: the access point declares `auth: basic`, so
        // `AuthScheme.BASIC.format` adds the "Basic " prefix on the way to the wire. Storing one
        // here produced `Authorization: Basic Basic <token>` and 401'd every authenticated call
        // while sign-in itself (an anonymous request) kept working.
        assertEquals("a2V5", prefs.authToken)
        assertTrue(loggedIn)
        job.cancel()
    }

    /**
     * A username of exactly the minimum length must be ACCEPTED.
     *
     * This is the boundary the authored replacement broke. `shortUsernameIsRejectedWithoutCallingTheServer`
     * above uses "abc" — 3 characters, refused by both the original rule (`< 4`) and the invented one
     * (`< 5`), so it passes either way and cannot see the difference. At exactly 4 the two rules
     * disagree: the original app signed such a user in, the rewrite locked them out.
     *
     * Asserted against `CredentialRules.MIN_USERNAME_LENGTH` rather than a literal 4, so the test
     * tracks the rule instead of a number someone can edit to make it pass.
     */
    @Test
    fun aUsernameAtExactlyTheMinimumLengthIsAccepted() = runTest(dispatcher) {
        val auth = FakeAuth(MutationResult.Applied(response(true), synced = true))
        val model = SignInViewModel(auth, preferences())
        model.actionChannel.trySend(
            SignInAction.UsernameChanged("u".repeat(CredentialRules.MIN_USERNAME_LENGTH)),
        )
        model.actionChannel.trySend(SignInAction.PasswordChanged("longenough"))
        model.actionChannel.trySend(SignInAction.Submit)
        runCurrent()
        assertNull(model.stateFlow.value.error, "a username at the minimum length must not be refused")
        assertEquals(1, auth.calls, "the server should have been called")
    }

    /** One character below the minimum is still refused, and without a server call. */
    @Test
    fun aUsernameOneBelowTheMinimumIsRefusedLocally() = runTest(dispatcher) {
        val auth = FakeAuth(MutationResult.Applied(response(true), synced = true))
        val model = SignInViewModel(auth, preferences())
        model.actionChannel.trySend(
            SignInAction.UsernameChanged("u".repeat(CredentialRules.MIN_USERNAME_LENGTH - 1)),
        )
        model.actionChannel.trySend(SignInAction.PasswordChanged("longenough"))
        model.actionChannel.trySend(SignInAction.Submit)
        runCurrent()
        assertEquals(SignInError.UsernameTooShort, model.stateFlow.value.error)
        assertEquals(0, auth.calls)
    }

    /** Same boundary for the password, against its own constant. */
    @Test
    fun aPasswordAtExactlyTheMinimumLengthIsAccepted() = runTest(dispatcher) {
        val auth = FakeAuth(MutationResult.Applied(response(true), synced = true))
        val model = SignInViewModel(auth, preferences())
        model.actionChannel.trySend(SignInAction.UsernameChanged("officer"))
        model.actionChannel.trySend(
            SignInAction.PasswordChanged("p".repeat(CredentialRules.MIN_PASSWORD_LENGTH)),
        )
        model.actionChannel.trySend(SignInAction.Submit)
        runCurrent()
        assertNull(model.stateFlow.value.error)
        assertEquals(1, auth.calls)
    }

    /**
     * The ViewModel must not carry its own copy of the thresholds.
     *
     * The regression existed because it did: a private `MIN_USERNAME = 5` sat beside the original's
     * `< 4`, with nothing relating them. Reading through [CredentialRules] is what makes the two
     * impossible to diverge again.
     */
    @Test
    fun thresholdsComeFromTheSharedRulesNotALocalConstant() = runTest(dispatcher) {
        val auth = FakeAuth(MutationResult.Applied(response(true), synced = true))
        val model = SignInViewModel(auth, preferences())
        val justBelow = "u".repeat(CredentialRules.MIN_USERNAME_LENGTH - 1)
        val atMinimum = "u".repeat(CredentialRules.MIN_USERNAME_LENGTH)

        model.actionChannel.trySend(SignInAction.UsernameChanged(justBelow))
        model.actionChannel.trySend(SignInAction.PasswordChanged("longenough"))
        model.actionChannel.trySend(SignInAction.Submit)
        runCurrent()
        assertEquals(SignInError.UsernameTooShort, model.stateFlow.value.error)

        model.actionChannel.trySend(SignInAction.UsernameChanged(atMinimum))
        model.actionChannel.trySend(SignInAction.Submit)
        runCurrent()
        assertNull(model.stateFlow.value.error, "the accept/reject edge must sit exactly on the shared constant")
    }
}
