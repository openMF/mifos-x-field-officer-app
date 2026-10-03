/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.loan

import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.loan.dao.LoanDao
import kpt.core.database.loan.entity.LoanDisburseTemplateCacheEntity
import kpt.core.database.loan.entity.LoanRepaymentTemplateEntity
import kpt.core.database.loan.entity.LoanTemplate
import kpt.core.database.loan.entity.LoanTemplateCacheEntity
import kpt.core.database.utils.getCurrentTimeInMillis
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.network.mifos.loan.api.LoanApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import kpt.core.network.mifos.loan.mapper.toDomain as disburseTemplateToDomain

private val templateJson = Json { ignoreUnknownKeys = true }

/** A loan application template is only valid for one (client, product) pair. */
data class LoanTemplateKey(val clientId: Int, val productId: Int?)

/**
 * The loan-application template — what the "new loan" form renders from.
 *
 * Cached because the officer fills this form in the field: without it, an application cannot be
 * started out of signal, which defeats the offline capture queue that would otherwise hold it.
 */
@StoreProvider(id = "loanTemplates", ttl = "12h")
@CacheKey(
    fn = "forClient",
    key = "loanTemplates:{clientId}:{productId}",
    params = ["clientId:Int", "productId:Int"],
)
fun provideLoanTemplateStore(
    service: LoanApi,
    dao: LoanDao,
): Store<LoanTemplateKey, LoanTemplate> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: LoanTemplateKey ->
        // NO_PRODUCT is the API's own "no product chosen yet" value, and the same sentinel the
        // composite primary key stores — one meaning, one constant.
        service.getLoansAccountTemplate(
            clientId = key.clientId,
            productId = key.productId ?: NO_PRODUCT,
        )
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: LoanTemplateKey ->
            // -1 stands in for "no product" so the pair can be a composite primary key.
            dao.observeLoanTemplate(key.clientId, key.productId ?: NO_PRODUCT).map { row ->
                row?.let { templateJson.decodeFromString<LoanTemplate>(it.payload) }
            }
        },
        writer = { key: LoanTemplateKey, template: LoanTemplate ->
            dao.upsertLoanTemplate(
                LoanTemplateCacheEntity(
                    clientId = key.clientId,
                    productId = key.productId ?: NO_PRODUCT,
                    payload = templateJson.encodeToString(template),
                    fetchedAtMs = getCurrentTimeInMillis(),
                ),
            )
        },
    ),
)

/** The repayment template — the payment-type options a repayment form needs, cached per loan. */
@StoreProvider(id = "loanRepaymentTemplates", ttl = "12h")
@CacheKey(fn = "forLoan", key = "loanRepaymentTemplates:{loanId}", params = ["loanId:Int"])
fun provideLoanRepaymentTemplateStore(
    service: LoanApi,
    dao: LoanDao,
): Store<Int, LoanRepaymentTemplateEntity> = StoreFactory.createStore(
    fetcher = Fetcher.of { loanId: Int -> service.getLoanRepaymentTemplate(loanId) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int -> dao.observeLoanRepaymentTemplate(loanId) },
        writer = { loanId: Int, template: LoanRepaymentTemplateEntity ->
            dao.saveLoanRepaymentTemplateFor(loanId, template)
        },
    ),
)

/** The disbursement template — cached so a disbursement can be prepared offline and queued. */
@StoreProvider(id = "loanDisburseTemplates", ttl = "12h")
@CacheKey(fn = "forLoan", key = "loanDisburseTemplates:{loanId}", params = ["loanId:Int"])
fun provideLoanDisburseTemplateStore(
    service: LoanApi,
    dao: LoanDao,
): Store<Int, LoanDisburseTemplate> = StoreFactory.createStore(
    fetcher = Fetcher.of { loanId: Int -> service.getDisburseTemplate(loanId).disburseTemplateToDomain() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int ->
            dao.observeLoanDisburseTemplate(loanId).map { row ->
                row?.let { templateJson.decodeFromString<LoanDisburseTemplate>(it.payload) }
            }
        },
        writer = { loanId: Int, template: LoanDisburseTemplate ->
            dao.upsertLoanDisburseTemplate(
                LoanDisburseTemplateCacheEntity(
                    loanId = loanId,
                    payload = templateJson.encodeToString(template),
                    fetchedAtMs = getCurrentTimeInMillis(),
                ),
            )
        },
    ),
)

private const val NO_PRODUCT = -1
