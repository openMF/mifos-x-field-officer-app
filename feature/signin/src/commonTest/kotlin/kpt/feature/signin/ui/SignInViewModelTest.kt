/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.security.BiometricAuthenticator
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeServerConfigRepository
import kpt.core.data.testing.FakeSessionRepository
import kpt.core.domain.validation.CredentialRules
import kpt.core.model.auth.PostAuthenticationRequest
import kpt.core.model.auth.PostAuthenticationResponse
import kpt.core.model.objects.users.User
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
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

        var resumed = 0
        override suspend fun resumeStoredSession(): MutationResult<Unit> {
            resumed++
            return MutationResult.Applied(Unit, synced = true)
        }

        override suspend fun signOut(clearLocalData: Boolean): MutationResult<Unit> = error("not used by sign-in")

        override suspend fun changePassword(
            newPassword: String,
            repeatPassword: String,
        ): MutationResult<Unit> = error("not used by sign-in")
    }

    /**
     * The biometric authenticator is the REAL expect/actual — it cannot be faked, because
     * `BiometricAuthenticator` is a final expect class with no interface above it.
     *
     * That is fine for every test here: the ViewModel's `init` wraps `isAvailable()` in
     * `runCatching`, and the unlock affordance additionally requires a STORED SESSION, which none
     * of these tests seeds. So the biometric path stays inert and these tests measure exactly the
     * credential behaviour they are about. Biometric unlock itself is device-verified territory,
     * recorded as such in docs.yaml#capabilities.
     */
    private fun signInViewModel(
        auth: AuthCommandRepository,
        session: FakeSessionRepository = FakeSessionRepository(),
    ) = SignInViewModel(auth, session, FakeServerConfigRepository(), BiometricAuthenticator())

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
        val model = signInViewModel(auth)
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
        val model = signInViewModel(FakeAuth(MutationResult.Blocked(BlockReason.OFFLINE)))
        submitValid(model)
        runCurrent()
        // Different problems: one is solved by finding signal, the other by retyping a password.
        assertEquals(SignInError.Offline, model.stateFlow.value.error)
    }

    @Test
    fun authenticatedFalseIsAFailedLoginEvenThoughTheCallSucceeded() = runTest(dispatcher) {
        // Fineract answers 200 with authenticated=false for a bad credential rather than a 4xx,
        // so a successful CALL is not a successful LOGIN.
        val model = signInViewModel(FakeAuth(MutationResult.Applied(response(false), synced = true)))
        submitValid(model)
        runCurrent()
        assertEquals(SignInError.Rejected, model.stateFlow.value.error)
    }

    @Test
    fun aStoredSessionIsWhatMakesBiometricUnlockRelevant() = runTest(dispatcher) {
        val prefs = FakeSessionRepository()
        val model = signInViewModel(FakeAuth(MutationResult.Applied(response(true), synced = true)), prefs)
        runCurrent()
        // Nothing stored yet. This is the first-run officer: there is no session to unlock INTO,
        // so the accelerator has nothing to accelerate.
        assertEquals(false, model.stateFlow.value.hasStoredSession)

        // A successful sign-in is exactly what creates the thing biometrics unlocks — and the
        // session is written by `AuthCommandRepository.authenticate`, not by this ViewModel, so the
        // test stores it the way that repository does. The flag follows the stored credential
        // rather than being set at sign-in, because the session survives a process death the
        // ViewModel does not.
        prefs.session.value = User(
            username = "officer",
            base64EncodedAuthenticationKey = "a2V5",
            isAuthenticated = true,
        )
        runCurrent()
        assertTrue(model.stateFlow.value.hasStoredSession)
    }

    @Test
    fun successSignalsTheShellAndLeavesPersistenceToTheRepository() = runTest(dispatcher) {
        val prefs = FakeSessionRepository()
        val model = signInViewModel(FakeAuth(MutationResult.Applied(response(true), synced = true)), prefs)
        var loggedIn = false
        val job = launch { model.eventFlow.collect { if (it is SignInEvent.LoggedIn) loggedIn = true } }
        submitValid(model)
        runCurrent()
        assertNull(model.stateFlow.value.error)
        assertTrue(loggedIn)
        // The ViewModel no longer writes the datastore: `AuthCommandRepository.authenticate`
        // persists the session as the command's local effect. The session contents (username,
        // raw key, no password, auth token) are asserted in core/data's AuthCommandRepositoryTest.
        assertNull(prefs.session.value.base64EncodedAuthenticationKey)
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
        val model = signInViewModel(auth)
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
        val model = signInViewModel(auth)
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
        val model = signInViewModel(auth)
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
        val model = signInViewModel(auth)
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

    @Test
    fun anUnreachableServerSaysSoAndDoesNotCountTowardTheLockout() = runTest(dispatcher) {
        // Measured on device 2026-10-25: a server with broken TLS told the officer to check their
        // credentials and burned a lockout attempt per tap.
        class SSLHandshakeException : RuntimeException("connection closed")
        val model = signInViewModel(FakeAuth(MutationResult.Failed(SSLHandshakeException(), rolledBack = false)))
        submitValid(model)
        runCurrent()
        assertEquals(SignInError.Unreachable, model.stateFlow.value.error)
        assertEquals(0, model.stateFlow.value.failedAttempts)
    }

    @Test
    fun aServerRejectionStillCountsTowardTheLockout() = runTest(dispatcher) {
        val rejected = MutationResult.Failed(RuntimeException("HTTP 401 Unauthorized"), rolledBack = false)
        val model = signInViewModel(FakeAuth(rejected))
        submitValid(model)
        runCurrent()
        assertEquals(SignInError.Rejected, model.stateFlow.value.error)
        assertEquals(1, model.stateFlow.value.failedAttempts)
    }

    @Test
    fun aDemoServerOffersItsPublishedSignInsAndFillsThePickedOne() = runTest(dispatcher) {
        // FakeServerConfigRepository's default IS its demo instance (two roles).
        val model = signInViewModel(FakeAuth(MutationResult.Applied(response(true), synced = true)))
        runCurrent()
        val accounts = model.stateFlow.value.demoAccounts
        assertEquals(listOf("demo", "boss"), accounts.map { it.username })
        model.actionChannel.trySend(SignInAction.UseDemoAccount(accounts[1]))
        runCurrent()
        assertEquals("boss", model.stateFlow.value.username)
        assertEquals("boss-pass", model.stateFlow.value.password)
    }
}
