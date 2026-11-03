/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.dataOrNull
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeCollectionSheetCommandRepository
import kpt.core.data.testing.FakeCollectionSheetRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.collectionsheet.ClientCollectionSheet
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.objects.account.loan.PaymentTypeOptions
import kpt.core.model.objects.collectionsheets.LoanCollectionSheet
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsAction
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsEvent
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsState
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class CollectionSheetDetailsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    // Two clients; Asha has TWO loans — the shape that broke the original's client-index lookup.
    private val sheet = IndividualCollectionSheet(
        dueDate = "24 October 2026",
        clients = arrayListOf(
            ClientCollectionSheet(
                clientId = 1,
                clientName = "Asha",
                loans = arrayListOf(
                    LoanCollectionSheet(
                        loanId = 11,
                        accountId = "000011",
                        productShortName = "GL",
                        totalDue = 100.0,
                        chargesDue = 5.0,
                    ),
                    LoanCollectionSheet(loanId = 12, accountId = "000012", productShortName = "EL", totalDue = 50.0),
                ),
            ),
            ClientCollectionSheet(
                clientId = 2,
                clientName = "Bimal",
                loans = arrayListOf(
                    LoanCollectionSheet(loanId = 21, accountId = "000021", productShortName = "GL", totalDue = 70.0),
                ),
            ),
        ),
        paymentTypeOptions = arrayListOf(PaymentTypeOptions(id = 3, name = "Cash")),
    )

    private val repository = FakeCollectionSheetRepository(ScreenState.Content(sheet))
    private val commands = FakeCollectionSheetCommandRepository()

    private fun TestScope.started(
        commands: FakeCollectionSheetCommandRepository = this@CollectionSheetDetailsViewModelTest.commands,
    ) =
        CollectionSheetDetailsViewModel(
            SavedStateHandle(mapOf("officeId" to 1, "staffId" to 4, "transactionDate" to "24 October 2026")),
            repository,
            commands,
        ).also { vm ->
            backgroundScope.launch { vm.sheet.collect {} }
            runCurrent()
        }

    private val CollectionSheetDetailsViewModel.rows get() = sheet.value.dataOrNull!!.rows

    @Test
    fun rowsAreLoansNotClients() = runTest(dispatcher) {
        val vm = started()
        assertEquals(listOf(11, 12, 21), vm.rows.map { it.loanId })
        assertEquals(listOf("Asha", "Asha", "Bimal"), vm.rows.map { it.clientName })
        // Defaults: due + charges, as the original computed.
        assertEquals(listOf(105.0, 50.0, 70.0), vm.rows.map { it.collect.transactionAmount })
        assertEquals(2, vm.sheet.value.dataOrNull!!.memberCount)
    }

    @Test
    fun theRequestIsTheRoutesAndCarriesTheDate() = runTest(dispatcher) {
        started()
        val request = repository.requested.single()
        assertEquals(1, request.officeId)
        assertEquals(4, request.staffId)
        assertEquals("24 October 2026", request.transactionDate)
    }

    @Test
    fun anEditedPaymentIsExactlyWhatIsSubmitted() = runTest(dispatcher) {
        val vm = started()
        vm.trySendAction(CollectionSheetDetailsAction.EditPayment(12))
        runCurrent()
        vm.trySendAction(
            CollectionSheetDetailsAction.EditorChanged {
                it.copy(amount = "30", paymentTypeId = 3, receiptNumber = " R-9 ")
            },
        )
        vm.trySendAction(CollectionSheetDetailsAction.SaveEditor)
        runCurrent()
        vm.trySendAction(CollectionSheetDetailsAction.Submit)
        runCurrent()

        // The ORIGINAL defect: the payment screen edited a route-JSON copy, so this was always the default.
        val sent = commands.saved.single().payload!!
        val edited = sent.bulkRepaymentTransactions.single { it.loanId == 12 }
        assertEquals(30.0, edited.transactionAmount)
        assertEquals(3, edited.paymentTypeId)
        assertEquals("R-9", edited.receiptNumber)
        // The other loans keep their defaults, and the date Fineract requires is sent.
        assertEquals(listOf(105.0, 30.0, 70.0), sent.bulkRepaymentTransactions.map { it.transactionAmount })
        assertEquals("24 October 2026", sent.transactionDate)
    }

    @Test
    fun aBadAmountIsRefusedNotSubmittedAsZero() = runTest(dispatcher) {
        val vm = started()
        vm.trySendAction(CollectionSheetDetailsAction.EditPayment(11))
        runCurrent()
        vm.trySendAction(CollectionSheetDetailsAction.EditorChanged { it.copy(amount = "1o0") })
        vm.trySendAction(CollectionSheetDetailsAction.SaveEditor)
        runCurrent()
        assertTrue(vm.stateFlow.value.editor!!.amountError)
        assertEquals(105.0, vm.rows.first().collect.transactionAmount)

        vm.trySendAction(CollectionSheetDetailsAction.EditorChanged { it.copy(amount = "-5") })
        vm.trySendAction(CollectionSheetDetailsAction.SaveEditor)
        runCurrent()
        assertTrue(vm.stateFlow.value.editor!!.amountError)
    }

    @Test
    fun paymentDetailsTravelOnlyWithAPaymentType() = runTest(dispatcher) {
        val vm = started()
        vm.trySendAction(CollectionSheetDetailsAction.EditPayment(21))
        runCurrent()
        vm.trySendAction(CollectionSheetDetailsAction.EditorChanged { it.copy(amount = "70", accountNumber = "A1") })
        vm.trySendAction(CollectionSheetDetailsAction.SaveEditor)
        runCurrent()
        assertNull(vm.rows.last().collect.accountNumber)
    }

    @Test
    fun aRejectionKeepsTheSheetAndSaysWhy() = runTest(dispatcher) {
        val failing = FakeCollectionSheetCommandRepository {
            MutationResult.Failed(
                fineractRejection("Repayment date is before the disbursement date"),
                rolledBack = false,
            )
        }
        val vm = started(failing)
        vm.trySendAction(CollectionSheetDetailsAction.Submit)
        runCurrent()
        assertEquals(
            CollectionSheetDetailsState.Dialog.Failed("Repayment date is before the disbursement date"),
            vm.stateFlow.value.dialog,
        )
        assertEquals(3, vm.rows.size) // still there to fix and resubmit
    }

    @Test
    fun aSavedSheetCannotBeSubmittedTwice() = runTest(dispatcher) {
        val vm = started()
        vm.trySendAction(CollectionSheetDetailsAction.Submit)
        runCurrent()
        assertEquals(CollectionSheetDetailsState.Dialog.Submitted, vm.stateFlow.value.dialog)
        vm.eventFlow.test {
            vm.trySendAction(CollectionSheetDetailsAction.DismissDialog)
            runCurrent()
            assertEquals(CollectionSheetDetailsEvent.NavigateBack, awaitItem())
        }
    }

    @Test
    fun nothingDueIsTheEmptyStateNotAnEmptyList() = runTest(dispatcher) {
        // A REAL arrived sheet with no loans — the device shape (one client with savings only). The
        // stream reports it as Content; the ViewModel must turn it into Empty.
        repository.sheet.value = ScreenState.Content(
            IndividualCollectionSheet(
                clients = arrayListOf(ClientCollectionSheet(clientId = 1, clientName = "Nadia Islam")),
            ),
        )
        val vm = started()
        assertEquals(ScreenState.Empty, vm.sheet.value)
    }
}
