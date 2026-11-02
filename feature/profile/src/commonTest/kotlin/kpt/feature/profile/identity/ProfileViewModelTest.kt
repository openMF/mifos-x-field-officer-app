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

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.auth.PasswordChangedSignInFailed
import kpt.core.data.staff.StaffRepository
import kpt.core.data.testing.FakeAuthCommandRepository
import kpt.core.data.testing.FakeDashboardRepository
import kpt.core.data.testing.FakeNetworkMonitor
import kpt.core.data.testing.FakeServerConfigRepository
import kpt.core.datastore.prefs.ProjectPreferencesRepositoryImpl
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.objects.users.User
import kpt.core.model.staff.Staff
import kpt.core.model.utils.getInstanceUrl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Profile (screens/profile): the session renders at once, `users/{id}` adds the name; the scope label
 * follows Home's rule; the password change validates, sends, and handles every outcome.
 *
 * Identities are the gateway's own (users/32 = IRVING ZEPEDA, no staff link, OFICIAL DE CAMPO).
 */
@OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)
class ProfileViewModelTest {

    @BeforeTest fun setUp() = Dispatchers.setMain(Dispatchers.Unconfined)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val dispatchers = object : DispatcherManager {
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: MainCoroutineDispatcher = Dispatchers.Main
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        override val appScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }

    private val session = User(
        username = "fieldofficer",
        userId = 32,
        isAuthenticated = true,
        officeId = 1,
        officeName = "CENTRAL",
    )

    private val irving = DashboardUser(
        id = 32,
        firstname = "IRVING",
        lastname = "ZEPEDA",
        officeId = 1,
        officeName = "CENTRAL",
        email = "oficial.campo@gmail.com",
        roles = listOf("OFICIAL DE CAMPO"),
    )

    private val dashboard = FakeDashboardRepository(user = irving)
    private val staffList = MutableStateFlow<ScreenState<List<Staff>>>(ScreenState.Content(emptyList()))
    private val staff = object : StaffRepository {
        override fun staffStream(officeId: Int, scope: CoroutineScope): ScreenDataStream<List<Staff>> =
            screenDataStreamForTesting(staffList)
    }
    private val network = FakeNetworkMonitor()
    private val auth = FakeAuthCommandRepository()
    private val servers = FakeServerConfigRepository()

    private suspend fun preferences() = run {
        val plain = MapSettings()
        val secure = MapSettings()
        ProjectPreferencesRepositoryImpl(
            delegate = UserPreferencesRepositoryImpl(plain, secure, dispatchers),
            plainSettings = plain,
            secureSettings = secure,
            dispatcher = dispatchers,
        ).also { it.updateFineractUser(session) }
    }

    private suspend fun TestScope.viewModel(): ProfileViewModel {
        val vm = ProfileViewModel(preferences(), servers, dashboard, staff, network, auth)
        backgroundScope.launch { vm.profile.collect {} }
        testScheduler.runCurrent()
        return vm
    }

    @Test
    fun theUserRecordAddsNameEmailAndRolesOverTheSession() = runTest {
        val p = viewModel().profile.value
        assertEquals("fieldofficer", p.username)
        assertEquals("IRVING ZEPEDA", p.fullName)
        assertEquals("IZ", p.initials)
        assertEquals("oficial.campo@gmail.com", p.email)
        assertEquals(listOf("OFICIAL DE CAMPO"), p.roles)
        assertEquals("CENTRAL", p.officeName)
        assertEquals(NameState.Loaded, p.name)
    }

    @Test
    fun whileTheNameLoadsTheSessionIdentityIsAlreadyThere() = runTest {
        dashboard.user.value = ScreenState.Loading
        val p = viewModel().profile.value
        assertEquals("fieldofficer", p.username)
        assertEquals("CENTRAL", p.officeName)
        assertEquals("F", p.initials)
        assertNull(p.fullName)
        assertEquals(NameState.Loading, p.name)
    }

    @Test
    fun aFailedNameReadKeepsTheIdentityAndRetries() = runTest {
        dashboard.user.value = ScreenState.Error(IllegalStateException("500"))
        val vm = viewModel()
        assertIs<NameState.Failed>(vm.profile.value.name)
        assertEquals("fieldofficer", vm.profile.value.username)

        dashboard.user.value = ScreenState.Content(irving)
        vm.trySendAction(ProfileAction.Retry)
        testScheduler.runCurrent()
        assertEquals("IRVING ZEPEDA", vm.profile.value.fullName)
    }

    @Test
    fun noStaffLinkMeansTheOfficeScopeAndNoStaffChip() = runTest {
        val p = viewModel().profile.value
        assertFalse(p.hasStaffLink)
        assertNull(p.isLoanOfficer)
    }

    @Test
    fun aStaffLinkedLoanOfficerIsMarkedSo() = runTest {
        dashboard.user.value = ScreenState.Content(irving.copy(staffId = 5, staffName = "Irving Zepeda"))
        staffList.value = ScreenState.Content(
            listOf(Staff(id = 4, isLoanOfficer = false), Staff(id = 5, isLoanOfficer = true)),
        )
        val p = viewModel().profile.value
        assertTrue(p.hasStaffLink)
        assertEquals(true, p.isLoanOfficer)
    }

    @Test
    fun theServerShownIsTheLiveConfigAndItsInstance() = runTest {
        val p = viewModel().profile.value
        assertEquals(FakeServerConfigRepository.TEST_DEFAULT.getInstanceUrl(), p.server.url)
        assertEquals("default", p.server.tenant)
        assertTrue(p.server.known)
    }

    @Test
    fun offlineIsReflected() = runTest {
        network.online.value = false
        assertFalse(viewModel().profile.value.online)
    }

    @Test
    fun saveIsOnlyPossibleWithTwoMatchingPasswords() = runTest {
        val vm = viewModel()
        vm.trySendAction(ProfileAction.OpenChangePassword)
        vm.trySendAction(ProfileAction.NewPassword("Field#Office2026"))
        vm.trySendAction(ProfileAction.RepeatPassword("Field#Office2027"))
        val form = vm.profile.value.password!!
        assertTrue(form.mismatch)
        assertFalse(form.canSave)

        vm.trySendAction(ProfileAction.SubmitPassword)
        assertTrue(auth.passwordChanges.isEmpty(), "a mismatched pair is never sent")

        vm.trySendAction(ProfileAction.RepeatPassword("Field#Office2026"))
        assertTrue(vm.profile.value.password!!.canSave)
    }

    @Test
    fun anAcceptedPasswordClosesTheSheetAndSaysSo() = runTest {
        val vm = viewModel()
        vm.enter("Field#Office2026")
        vm.trySendAction(ProfileAction.SubmitPassword)
        testScheduler.runCurrent()

        assertEquals(listOf("Field#Office2026" to "Field#Office2026"), auth.passwordChanges)
        assertNull(vm.profile.value.password)
        assertEquals(ProfileEvent.PasswordChanged, vm.eventFlow.first())
    }

    @Test
    fun aServerRefusalShowsItsReasonAndKeepsTheSheet() = runTest {
        auth.changePasswordResult = MutationResult.Failed(IllegalStateException("400"), rolledBack = false)
        val vm = viewModel()
        vm.enter("abc")
        vm.trySendAction(ProfileAction.SubmitPassword)
        testScheduler.runCurrent()

        val form = vm.profile.value.password!!
        assertIs<PasswordError.Server>(form.error)
        assertFalse(form.submitting)
    }

    @Test
    fun offlineSubmitSaysToConnect() = runTest {
        auth.changePasswordResult = MutationResult.Blocked(BlockReason.OFFLINE)
        val vm = viewModel()
        vm.enter("Field#Office2026")
        vm.trySendAction(ProfileAction.SubmitPassword)
        testScheduler.runCurrent()
        assertEquals(PasswordError.Offline, vm.profile.value.password!!.error)
    }

    @Test
    fun whenSigningInAgainFailsTheSheetCloses() = runTest {
        // The repository already ended the session (data kept); the root graph goes to sign-in.
        auth.changePasswordResult = MutationResult.Failed(PasswordChangedSignInFailed(), rolledBack = false)
        val vm = viewModel()
        vm.enter("Field#Office2026")
        vm.trySendAction(ProfileAction.SubmitPassword)
        testScheduler.runCurrent()
        assertNull(vm.profile.value.password)
    }

    private fun ProfileViewModel.enter(password: String) {
        trySendAction(ProfileAction.OpenChangePassword)
        trySendAction(ProfileAction.NewPassword(password))
        trySendAction(ProfileAction.RepeatPassword(password))
    }
}
