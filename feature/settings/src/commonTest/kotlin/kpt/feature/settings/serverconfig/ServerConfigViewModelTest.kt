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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeAuthCommandRepository
import kpt.core.data.testing.FakeServerConfigRepository
import kpt.core.data.testing.FakeSessionRepository
import kpt.core.domain.validation.ValidationFailure
import kpt.core.model.objects.users.User
import kpt.core.model.utils.ServerConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the server-config editor's validation and save behaviour.
 *
 * The screen it replaces (`6b66e8a43:.../updateServer/`) validated only reactively, per keystroke,
 * and had no combined view of validity — so nothing stopped `UpdateServerConfig` from writing while
 * a field was invalid. The submit-path tests below are the ones that would have caught that.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class ServerConfigViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val auth = FakeAuthCommandRepository()

    /** A model whose stored config has arrived. Read what the screen shows from [ServerConfigViewModel.form]. */
    private fun kotlinx.coroutines.test.TestScope.newModel(
        repository: FakeServerConfigRepository = FakeServerConfigRepository(),
        session: FakeSessionRepository = FakeSessionRepository(),
    ) = ServerConfigViewModel(repository, auth, session).also { runCurrent() }

    private val signedIn get() = FakeSessionRepository(User(username = "officer", isAuthenticated = true))

    @Test
    fun theDefaultConfigIsValid() = runTest(dispatcher) {
        val model = newModel()
        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("test"))
        runCurrent()
        assertEquals(emptyMap(), model.form.value.failures, "the default must pass its own rules")
        assertTrue(model.form.value.isValid)
    }

    @Test
    fun theLocalhostPresetIsAlsoValid() = runTest(dispatcher) {
        val model = newModel()
        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        runCurrent()
        assertEquals(emptyMap(), model.form.value.failures, "ServerConfig.LOCALHOST must pass its own rules")
    }

    @Test
    fun aHostNameWithASchemeIsRejectedOnTheEndpointField() = runTest(dispatcher) {
        val model = newModel()
        model.actionChannel.trySend(ServerConfigAction.EndPointChanged("https://demo.mifos.org"))
        runCurrent()
        // The scheme belongs in the protocol field; attributing it to Endpoint is what tells the user where.
        assertEquals(
            ValidationFailure.MalformedHostname,
            model.form.value.failures[ServerConfigField.EndPoint],
        )
    }

    @Test
    fun anOutOfRangePortIsAttributedToThePortField() = runTest(dispatcher) {
        val model = newModel()
        model.actionChannel.trySend(ServerConfigAction.PortChanged("70000"))
        runCurrent()
        val failure = model.form.value.failures[ServerConfigField.Port]
        assertTrue(failure is ValidationFailure.PortOutOfRange, "expected PortOutOfRange, got $failure")
    }

    @Test
    fun anApiPathMissingItsTrailingSlashIsDistinguishedFromAMissingLeadingSlash() = runTest(dispatcher) {
        val model = newModel()

        model.actionChannel.trySend(ServerConfigAction.ApiPathChanged("/fineract-provider/api/v1"))
        runCurrent()
        assertEquals(
            ValidationFailure.ApiPathMissingTrailingSlash,
            model.form.value.failures[ServerConfigField.ApiPath],
        )

        model.actionChannel.trySend(ServerConfigAction.ApiPathChanged("fineract-provider/api/v1/"))
        runCurrent()
        assertEquals(
            ValidationFailure.ApiPathMissingLeadingSlash,
            model.form.value.failures[ServerConfigField.ApiPath],
        )
    }

    /**
     * The test the original could not have had: submit must refuse while any field is invalid.
     *
     * The original computed five independent error flows and no aggregate, so `UpdateServerConfig`
     * wrote whatever was in the fields regardless.
     */
    @Test
    fun submitWithAnInvalidFieldDoesNotPersistAnything() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val before = prefs.saved.value
        val model = newModel(prefs)

        model.actionChannel.trySend(ServerConfigAction.TenantChanged("not valid!"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()

        assertEquals(before, prefs.saved.value, "an invalid config must not be persisted")
        assertEquals(
            ValidationFailure.MalformedTenant,
            model.form.value.failures[ServerConfigField.Tenant],
        )
    }

    @Test
    fun submitWithAValidConfigPersistsIt() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val model = newModel(prefs)

        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()

        assertEquals(ServerConfig.LOCALHOST, prefs.saved.value)
    }

    /**
     * Submitting without touching a field still surfaces why it refused.
     *
     * Validation is computed on edit, so a never-edited invalid config would otherwise submit with an
     * empty `failures` map and fail silently.
     */
    @Test
    fun submitRepublishesFailuresEvenWhenNoFieldWasEdited() = runTest(dispatcher) {
        val model = newModel(
            FakeServerConfigRepository(initial = FakeServerConfigRepository.TEST_DEFAULT.copy(port = "0")),
        )
        assertEquals(emptyMap(), model.form.value.failures, "nothing validated yet")

        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()

        assertTrue(
            model.form.value.failures[ServerConfigField.Port] is ValidationFailure.PortOutOfRange,
            "submit must publish the failure it refused on",
        )
    }

    @Test
    fun theFieldsAreSeededWithTheStoredConfig() = runTest(dispatcher) {
        val stored = FakeServerConfigRepository.TEST_DEFAULT.copy(endPoint = "branch.example.org", tenant = "branch")
        assertEquals(stored, newModel(FakeServerConfigRepository(initial = stored)).form.value.config)
    }

    @Test
    fun aFailedSaveIsReportedNotClaimedAsSaved() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository().apply { failSaves = true }
        val model = newModel(prefs)

        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()

        assertEquals(ServerConfigEvent.SaveFailed, model.eventFlow.first())
        assertEquals(FakeServerConfigRepository.TEST_DEFAULT, prefs.saved.value)
    }

    /**
     * A session belongs to the server that issued it. The original signed out before saving a new
     * server (`prefManager.logOut()`); that is kept — with local data KEPT, so unsynced work survives.
     */
    @Test
    fun savingADifferentServerSignsOutKeepingLocalData() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val model = newModel(prefs)
        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()
        assertEquals(listOf(false), auth.signOuts)
        assertEquals(ServerConfigEvent.Saved, model.eventFlow.first())
    }

    @Test
    fun resavingTheSameServerDoesNotSignOut() = runTest(dispatcher) {
        val model = newModel()
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()
        assertEquals(emptyList(), auth.signOuts)
    }

    @Test
    fun anEditIsNotOverwrittenWhenTheStoredConfigReEmits() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val model = newModel(prefs)
        model.actionChannel.trySend(ServerConfigAction.TenantChanged("branch"))
        runCurrent()
        prefs.saved.value = FakeServerConfigRepository.TEST_DEFAULT.copy(endPoint = "elsewhere.example.org")
        runCurrent()
        assertEquals("branch", model.form.value.config.tenant)
    }

    @Test
    fun pickingAnInstanceFillsEveryFieldAndExposesItsDemoCredentials() = runTest(dispatcher) {
        val model = newModel(FakeServerConfigRepository(initial = ServerConfig.LOCALHOST))
        assertEquals("localhost", model.form.value.selectedInstance?.id)
        assertEquals(false, model.form.value.selectedInstance?.isDemo, "localhost is not a demo instance")

        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("test"))
        runCurrent()
        assertEquals(FakeServerConfigRepository.TEST_DEFAULT, model.form.value.config)
        assertEquals(listOf("demo", "boss"), model.form.value.selectedInstance?.demoAccounts?.map { it.username })
    }

    @Test
    fun editingAFieldAwayFromEveryInstanceIsACustomServer() = runTest(dispatcher) {
        val model = newModel()
        model.actionChannel.trySend(ServerConfigAction.TenantChanged("branch"))
        runCurrent()
        assertNull(model.form.value.selectedInstance)
    }

    @Test
    fun signedInSwitchingServerAsksFirstAndSavesNothingUntilConfirmed() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val model = newModel(prefs, signedIn)
        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()
        assertTrue(model.form.value.confirmingSwitch)
        assertEquals(FakeServerConfigRepository.TEST_DEFAULT, prefs.saved.value, "nothing saved before confirming")
        assertEquals(emptyList(), auth.signOuts)

        model.actionChannel.trySend(ServerConfigAction.SwitchConfirmed)
        runCurrent()
        assertEquals(ServerConfig.LOCALHOST, prefs.saved.value)
        assertEquals(listOf(false), auth.signOuts)
        assertFalse(model.form.value.confirmingSwitch)
    }

    @Test
    fun cancellingTheSwitchKeepsTheServerAndTheSession() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val model = newModel(prefs, signedIn)
        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        model.actionChannel.trySend(ServerConfigAction.SwitchCancelled)
        runCurrent()
        assertFalse(model.form.value.confirmingSwitch)
        assertEquals(FakeServerConfigRepository.TEST_DEFAULT, prefs.saved.value)
        assertEquals(emptyList(), auth.signOuts)
    }

    @Test
    fun signedOutSavesWithoutAsking() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        val model = newModel(prefs)
        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        model.actionChannel.trySend(ServerConfigAction.Submit)
        runCurrent()
        assertFalse(model.form.value.confirmingSwitch)
        assertEquals(ServerConfig.LOCALHOST, prefs.saved.value)
    }

    @Test
    fun thePickerShowsLoadingUntilTheListLandsAndTheFieldsStayUsable() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        prefs.instancesState.value = ScreenState.Loading
        val model = newModel(prefs)
        assertEquals(ScreenState.Loading, model.instances.value)
        assertEquals(emptyList(), model.form.value.instances, "no instances to pick yet")
        assertEquals(
            FakeServerConfigRepository.TEST_DEFAULT,
            model.form.value.config,
            "the fields still show the server",
        )

        prefs.instancesState.value = ScreenState.Content(FakeServerConfigRepository.TEST_INSTANCES)
        runCurrent()
        assertEquals("test", model.form.value.selectedInstance?.id)
    }

    @Test
    fun aFailedLoadCanBeRetried() = runTest(dispatcher) {
        val prefs = FakeServerConfigRepository()
        prefs.instancesState.value = ScreenState.Error(RuntimeException("offline"))
        val model = newModel(prefs)
        assertTrue(model.instances.value is ScreenState.Error)
        model.actionChannel.trySend(ServerConfigAction.RetryInstances)
        prefs.instancesState.value = ScreenState.Content(FakeServerConfigRepository.TEST_INSTANCES)
        runCurrent()
        assertTrue(model.instances.value is ScreenState.Content)
    }

    @Test
    fun aSignedInSessionShowsTheLiveServerAndWhoIsSignedIn() = runTest(dispatcher) {
        val model = newModel(session = signedIn)
        val live = model.form.value.live

        assertEquals(model.form.value.config, live, "live is the SAVED config")
        assertEquals("officer", model.form.value.liveUsername)
    }

    @Test
    fun editingTheFieldsNeverMovesTheLiveServer() = runTest(dispatcher) {
        // Live is what the session runs on — a draft is not live until the switch is confirmed.
        val model = newModel(session = signedIn)
        val live = model.form.value.live

        model.actionChannel.trySend(ServerConfigAction.InstanceSelected("localhost"))
        runCurrent()

        assertEquals(live, model.form.value.live)
        assertTrue(model.form.value.config != live, "the draft moved")
    }

    @Test
    fun signInsCopyOfTheScreenHasNoLiveServer() = runTest(dispatcher) {
        assertEquals(null, newModel().form.value.live)
    }
}
