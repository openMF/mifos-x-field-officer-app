/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.loan.LoanRepository
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.template.loan.GroupLoanTemplate

/**
 * Test double for [LoanRepository] — same contract-owner reasoning as [FakeClientRepository]: the
 * module that declares the interface owns its fake, so adding a stream breaks it in ONE place
 * instead of in every anonymous `object : LoanRepository` scattered across feature tests.
 *
 * Three screens now share `loanStream` (profile, schedule, transactions) because the schedule and
 * the transactions ARE part of `LoanWithAssociations` — so a fake that hands all three the same
 * supplied flow is not a convenience here, it is the production shape.
 *
 * Unimplemented streams THROW. See [FakeClientRepository]'s KDoc for why that is deliberate.
 */
@ExperimentalDataTestingApi
class FakeLoanRepository(
    private val loan: MutableStateFlow<ScreenState<LoanWithAssociations>>? = null,
    private val loanTemplate: MutableStateFlow<ScreenState<LoanTemplate>>? = null,
    private val repaymentTemplate: MutableStateFlow<ScreenState<LoanRepaymentTemplate>>? = null,
    private val disburseTemplate: MutableStateFlow<ScreenState<LoanDisburseTemplate>>? = null,
    /**
     * Answers the cascading template question. Receives the three TO-side values so a test can
     * model the cascade narrowing — or return null to model the read failing.
     */
    private val transferTemplate: (
        toOfficeId: Int?,
        toClientId: Int?,
        toAccountType: Int?,
    ) -> AccountTransferTemplate? = { _, _, _ -> null },
    private val loanReschedules: MutableStateFlow<ScreenState<List<LoanRescheduleResponse>>>? = null,
    /** Answers the propose form's reason-catalog question; null models the read failing. */
    private val rescheduleReasons: () -> LoanRescheduleTemplate? = { null },
    private val loanProducts: MutableStateFlow<ScreenState<List<LoanProducts>>>? = null,
    /** Answers the group-loan template question, per PRODUCT; null models the read failing. */
    private val groupLoanTemplate: (productId: Int) -> GroupLoanTemplate? = { null },
    /**
     * Answers the individual-loan template question, per PRODUCT; null models the read failing.
     *
     * Named apart from the `loanTemplate` STREAM above because the two are different reads of the
     * same endpoint: the stream is keyed at construction (and has no production caller), the
     * one-shot is what the new-application wizard cascades through.
     */
    private val individualLoanTemplate: (productId: Int) -> LoanTemplate? = { null },
) : LoanRepository {

    private val triggers: MutableMap<String, MutableSharedFlow<Unit>> = mutableMapOf()

    /** The refresh signal of one stream, so a test can see it re-read. */
    fun refreshesFor(stream: String): MutableSharedFlow<Unit> =
        triggers.getOrPut(stream) { MutableSharedFlow(extraBufferCapacity = 8) }

    /** Every stream's refresh signal. */
    val refreshes: MutableSharedFlow<Unit>
        get() = triggers.values.singleOrNull()
            ?: error("several streams are wired; use refreshesFor(<stream>) instead (${triggers.keys})")

    /** Every key each stream was asked for — proves the route argument arrived. */
    val requestedKeys: MutableMap<String, MutableList<Any>> = mutableMapOf()

    private fun <T> stream(
        name: String,
        key: Any?,
        flow: MutableStateFlow<ScreenState<T>>?,
    ): ScreenDataStream<T> {
        val source = requireNotNull(flow) {
            "FakeLoanRepository: $name was read but no state flow was supplied for it"
        }
        if (key != null) requestedKeys.getOrPut(name) { mutableListOf() } += key
        return screenDataStreamForTesting(source, refreshTrigger = refreshesFor(name))
    }

    override fun loanStream(loanId: Int, scope: CoroutineScope) = stream("loan", loanId, loan)

    override fun loanTemplateStream(clientId: Int, productId: Int?, scope: CoroutineScope) =
        stream("loanTemplate", clientId to productId, loanTemplate)

    override fun repaymentTemplateStream(loanId: Int, scope: CoroutineScope) =
        stream("repaymentTemplate", loanId, repaymentTemplate)

    override fun disburseTemplateStream(loanId: Int, scope: CoroutineScope) =
        stream("disburseTemplate", loanId, disburseTemplate)

    override fun loanReschedulesStream(loanId: Int, scope: CoroutineScope) =
        stream("loanReschedules", loanId, loanReschedules)

    override fun loanProductsStream(scope: CoroutineScope) =
        stream("loanProducts", null, loanProducts)

    /** Every (client, product) pair the individual template was asked for. */
    val loanTemplateQueries: MutableList<Pair<Int, Int>> = mutableListOf()

    override suspend fun loanTemplate(clientId: Int, productId: Int): LoanTemplate? {
        loanTemplateQueries += clientId to productId
        return individualLoanTemplate(productId)
    }

    /** Every (group, product) pair the template was asked for — proves the cascade re-asked. */
    val groupLoanTemplateQueries: MutableList<Pair<Int, Int>> = mutableListOf()

    override suspend fun groupLoanTemplate(groupId: Int, productId: Int): GroupLoanTemplate? {
        groupLoanTemplateQueries += groupId to productId
        return groupLoanTemplate(productId)
    }

    /** How many times the reason catalog was asked for — it is a catalog, so once should do. */
    var rescheduleReasonQueries: Int = 0
        private set

    override suspend fun loanRescheduleReasons(): LoanRescheduleTemplate? {
        rescheduleReasonQueries++
        return rescheduleReasons()
    }

    /** Every template question asked, in order — a test asserts the CASCADE narrowed correctly. */
    val transferTemplateQueries: MutableList<TransferTemplateQuery> = mutableListOf()

    override suspend fun accountTransferTemplate(
        fromClientId: Int,
        fromAccountId: Int,
        fromAccountType: Int,
        fromOfficeId: Int?,
        toOfficeId: Int?,
        toClientId: Int?,
        toAccountType: Int?,
    ): AccountTransferTemplate? {
        transferTemplateQueries += TransferTemplateQuery(
            fromClientId = fromClientId,
            fromAccountId = fromAccountId,
            fromAccountType = fromAccountType,
            fromOfficeId = fromOfficeId,
            toOfficeId = toOfficeId,
            toClientId = toClientId,
            toAccountType = toAccountType,
        )
        return transferTemplate(toOfficeId, toClientId, toAccountType)
    }
}

/** One `accountTransferTemplate` call, recorded so a test can assert the cascade's questions. */
@ExperimentalDataTestingApi
data class TransferTemplateQuery(
    /** The client transferred from. */
    val fromClientId: Int,
    /** The account transferred from. */
    val fromAccountId: Int,
    /** The type of the account transferred from. */
    val fromAccountType: Int,
    /** The office transferred from, once chosen. */
    val fromOfficeId: Int?,
    /** The office transferred to, once chosen. */
    val toOfficeId: Int?,
    /** The client transferred to, once chosen. */
    val toClientId: Int?,
    /** The type of the account transferred to, once chosen. */
    val toAccountType: Int?,
)
