/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.queue

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.center.dao.CenterDao
import kpt.core.database.client.dao.ClientDao
import kpt.core.database.group.dao.GroupsDao
import kpt.core.database.loan.dao.LoanDao
import kpt.core.database.savings.dao.SavingsDao
import kpt.core.model.center.CenterPayload
import kpt.core.model.client.ClientPayload
import kpt.core.model.group.GroupPayload
import kpt.core.model.loan.LoanRepaymentRequest
import kpt.core.model.savings.SavingsAccountTransactionRequest
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The offline WRITE half of the field-officer app: work captured with no connectivity.
 *
 * A field officer enrols a client under a tree and records a repayment in a village with no signal;
 * the entry has to land locally and survive a restart, then reach Fineract later. Each of these is
 * `createOfflineMutableStore` — the write completes against Room with NO network leg, because the
 * network leg is an explicit, user-visible sync of the pending queue, not an implicit background
 * post. That is the difference between "saved" and "sent", and the officer has to be able to see it.
 *
 * Every one declares `logout = false`, which is NOT a privacy hole: Store5 5.1 keeps `MutableStore`
 * outside the `Store` hierarchy, so `StoreCacheManager.register(store: Store<*, *>)` cannot take
 * one — and because the generated registration block is `createdAtStart = true`, leaving the
 * default `logout = true` makes Koin miss the binding and the app die on launch, not on logout.
 * WHEN THEY ARE PURGED (decided 2026-10-23). The sign-out dialog offers two choices:
 *  - "Sign out only" keeps everything, queued captures included — a capture made with no signal is
 *    work the officer has not been able to send yet, and the next sign-in must still have it.
 *  - "Sign out and clear data" runs `StoreCacheManager.clearAll()`, which reaches each table ONLY
 *    through a store's SourceOfTruth `deleteAll`. So every `pending*` read store below declares one,
 *    and the queued rows go with everything else. The dialog says so before the officer confirms.
 * Before this, neither happened: `clearAll()` had no caller, and no `pending*` store had `deleteAll`.
 *
 * Keys are the queue row's own id, so a repository can address one pending item to retry or discard
 * it. `core/data` drives these through `MutationGateway.upsert(...)`; it must never touch the DAO
 * directly (S5-1 mutations-bypass-store).
 */
// logout-exempt: MutableStore; "Sign out and clear data" clears it via its pending* read store
@StoreProvider(id = "clientPayloadQueue", logout = false)
@CacheKey(fn = "byId", key = "clientPayloadQueue:{id}", params = ["id:Int"])
fun provideClientPayloadQueueStore(
    dao: ClientDao,
): MutableStore<Int, ClientPayload> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeClientPayload(id).map { it?.toModel() } },
        writer = { _: Int, payload: ClientPayload -> dao.insertClientPayload(payload.toEntity()) },
        delete = { id: Int -> dao.deleteClientPayloadById(id) },
    ),
)

// logout-exempt: MutableStore; "Sign out and clear data" clears it via its pending* read store
@StoreProvider(id = "centerPayloadQueue", logout = false)
@CacheKey(fn = "byId", key = "centerPayloadQueue:{id}", params = ["id:Int"])
fun provideCenterPayloadQueueStore(
    dao: CenterDao,
): MutableStore<Int, CenterPayload> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeCenterPayload(id).map { it?.toModel() } },
        writer = { _: Int, payload: CenterPayload -> dao.saveCenterPayload(payload.toEntity()) },
        delete = { id: Int -> dao.deleteCenterPayloadById(id) },
    ),
)

// logout-exempt: MutableStore; "Sign out and clear data" clears it via its pending* read store
@StoreProvider(id = "groupPayloadQueue", logout = false)
@CacheKey(fn = "byId", key = "groupPayloadQueue:{id}", params = ["id:Int"])
fun provideGroupPayloadQueueStore(
    dao: GroupsDao,
): MutableStore<Int, GroupPayload> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeGroupPayload(id).map { it?.toModel() } },
        writer = { _: Int, payload: GroupPayload -> dao.insertGroupPayload(payload.toEntity()) },
        delete = { id: Int -> dao.deleteGroupPayloadById(id) },
    ),
)

/** Keyed by loan, not by the table's `timeStamp` primary key — see `observeLoanRepaymentRequest`. */

// logout-exempt: MutableStore; "Sign out and clear data" clears it via its pending* read store
@StoreProvider(id = "loanRepaymentQueue", logout = false)
@CacheKey(fn = "byLoan", key = "loanRepaymentQueue:{loanId}", params = ["loanId:Int"])
fun provideLoanRepaymentQueueStore(
    dao: LoanDao,
): MutableStore<Int, LoanRepaymentRequest> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int -> dao.observeLoanRepaymentRequest(loanId).map { it?.toModel() } },
        writer = { loanId: Int, request: LoanRepaymentRequest ->
            dao.saveLoanRepaymentTransaction(loanId, request.toEntity())
        },
        delete = { loanId: Int -> dao.deleteLoanRepaymentRequestByLoanId(loanId) },
    ),
)

// logout-exempt: MutableStore; "Sign out and clear data" clears it via its pending* read store
@StoreProvider(id = "savingsTransactionQueue", logout = false)
@CacheKey(fn = "byAccount", key = "savingsTransactionQueue:{accountId}", params = ["accountId:Int"])
fun provideSavingsTransactionQueueStore(
    dao: SavingsDao,
): MutableStore<Int, SavingsAccountTransactionRequest> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { accountId: Int -> dao.getSavingsAccountTransactionRequest(accountId).map { it?.toModel() } },
        writer = { _: Int, request: SavingsAccountTransactionRequest ->
            dao.insertSavingsAccountTransactionRequest(request.toEntity())
        },
        delete = { accountId: Int -> dao.deleteSavingsAccountTransactionRequest(accountId) },
    ),
)

// ── Pending-queue reads ──────────────────────────────────────────────────────────
//
// What the sync screen lists: everything captured offline and not yet accepted by the server.
// `createOfflineStore` because there is no server-side view of these — they exist only until the
// sync drains them, so a fetcher would have nothing to call.

@StoreProvider(id = "pendingClientPayloads")
@CacheKey(name = "LIST", key = "pendingClientPayloads")
fun providePendingClientPayloadsStore(
    dao: ClientDao,
): Store<Unit, List<ClientPayload>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllClientPayload().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, _: List<ClientPayload> -> Unit },
        deleteAll = { dao.deleteAllClientPayloads() },
    ),
)

@StoreProvider(id = "pendingCenterPayloads")
@CacheKey(name = "LIST", key = "pendingCenterPayloads")
fun providePendingCenterPayloadsStore(
    dao: CenterDao,
): Store<Unit, List<CenterPayload>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.readAllCenterPayload().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, _: List<CenterPayload> -> Unit },
        deleteAll = { dao.deleteAllCenterPayloads() },
    ),
)

@StoreProvider(id = "pendingGroupPayloads")
@CacheKey(name = "LIST", key = "pendingGroupPayloads")
fun providePendingGroupPayloadsStore(
    dao: GroupsDao,
): Store<Unit, List<GroupPayload>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllGroupPayloads().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, _: List<GroupPayload> -> Unit },
        deleteAll = { dao.deleteAllGroupPayloads() },
    ),
)

@StoreProvider(id = "pendingLoanRepayments")
@CacheKey(name = "LIST", key = "pendingLoanRepayments")
fun providePendingLoanRepaymentsStore(
    dao: LoanDao,
): Store<Unit, List<LoanRepaymentRequest>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.readAllLoanRepaymentTransaction().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, _: List<LoanRepaymentRequest> -> Unit },
        deleteAll = { dao.deleteAllLoanRepaymentRequests() },
    ),
)

@StoreProvider(id = "pendingSavingsTransactions")
@CacheKey(name = "LIST", key = "pendingSavingsTransactions")
fun providePendingSavingsTransactionsStore(
    dao: SavingsDao,
): Store<Unit, List<SavingsAccountTransactionRequest>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllSavingsAccountTransactionRequest().map { rows -> rows.map { it.toModel() } } },
        writer = { _: Unit, _: List<SavingsAccountTransactionRequest> -> Unit },
        deleteAll = { dao.deleteAllSavingsAccountTransactionRequests() },
    ),
)
