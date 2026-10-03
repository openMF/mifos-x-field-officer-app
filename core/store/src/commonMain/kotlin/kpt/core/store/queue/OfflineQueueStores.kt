/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.queue

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.center.dao.CenterDao
import kpt.core.database.center.entity.CenterPayloadEntity
import kpt.core.database.client.dao.ClientDao
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.database.group.dao.GroupsDao
import kpt.core.database.group.entity.GroupPayloadEntity
import kpt.core.database.loan.dao.LoanDao
import kpt.core.database.loan.entity.LoanRepaymentRequestEntity
import kpt.core.database.savings.dao.SavingsDao
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity
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
 * The rows still get purged: each queue shares its table with the paired `pending*` read store
 * below, which IS registered. That pairing is what makes `logout = false` safe here — a queued
 * payload belongs to the officer who captured it and must not survive into another session.
 *
 * Keys are the queue row's own id, so a repository can address one pending item to retry or discard
 * it. `core/data` drives these through `MutationGateway.upsert(...)`; it must never touch the DAO
 * directly (S5-1 mutations-bypass-store).
 */

@StoreProvider(id = "clientPayloadQueue", logout = false)
@CacheKey(fn = "byId", key = "clientPayloadQueue:{id}", params = ["id:Int"])
fun provideClientPayloadQueueStore(
    dao: ClientDao,
): MutableStore<Int, ClientPayloadEntity> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeClientPayload(id) },
        writer = { _: Int, payload: ClientPayloadEntity -> dao.insertClientPayload(payload) },
        delete = { id: Int -> dao.deleteClientPayloadById(id) },
    ),
)

@StoreProvider(id = "centerPayloadQueue", logout = false)
@CacheKey(fn = "byId", key = "centerPayloadQueue:{id}", params = ["id:Int"])
fun provideCenterPayloadQueueStore(
    dao: CenterDao,
): MutableStore<Int, CenterPayloadEntity> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeCenterPayload(id) },
        writer = { _: Int, payload: CenterPayloadEntity -> dao.saveCenterPayload(payload) },
        delete = { id: Int -> dao.deleteCenterPayloadById(id) },
    ),
)

@StoreProvider(id = "groupPayloadQueue", logout = false)
@CacheKey(fn = "byId", key = "groupPayloadQueue:{id}", params = ["id:Int"])
fun provideGroupPayloadQueueStore(
    dao: GroupsDao,
): MutableStore<Int, GroupPayloadEntity> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { id: Int -> dao.observeGroupPayload(id) },
        writer = { _: Int, payload: GroupPayloadEntity -> dao.insertGroupPayload(payload) },
        delete = { id: Int -> dao.deleteGroupPayloadById(id) },
    ),
)

/** Keyed by loan, not by the table's `timeStamp` primary key — see `observeLoanRepaymentRequest`. */
@StoreProvider(id = "loanRepaymentQueue", logout = false)
@CacheKey(fn = "byLoan", key = "loanRepaymentQueue:{loanId}", params = ["loanId:Int"])
fun provideLoanRepaymentQueueStore(
    dao: LoanDao,
): MutableStore<Int, LoanRepaymentRequestEntity> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int -> dao.observeLoanRepaymentRequest(loanId) },
        writer = { loanId: Int, request: LoanRepaymentRequestEntity ->
            dao.saveLoanRepaymentTransaction(loanId, request)
        },
        delete = { loanId: Int -> dao.deleteLoanRepaymentByLoanId(loanId) },
    ),
)

@StoreProvider(id = "savingsTransactionQueue", logout = false)
@CacheKey(fn = "byAccount", key = "savingsTransactionQueue:{accountId}", params = ["accountId:Int"])
fun provideSavingsTransactionQueueStore(
    dao: SavingsDao,
): MutableStore<Int, SavingsAccountTransactionRequestEntity> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { accountId: Int -> dao.getSavingsAccountTransactionRequest(accountId) },
        writer = { _: Int, request: SavingsAccountTransactionRequestEntity ->
            dao.insertSavingsAccountTransactionRequest(request)
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
): Store<Unit, List<ClientPayloadEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllClientPayload() },
        writer = { _: Unit, _: List<ClientPayloadEntity> -> Unit },
    ),
)

@StoreProvider(id = "pendingCenterPayloads")
@CacheKey(name = "LIST", key = "pendingCenterPayloads")
fun providePendingCenterPayloadsStore(
    dao: CenterDao,
): Store<Unit, List<CenterPayloadEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.readAllCenterPayload() },
        writer = { _: Unit, _: List<CenterPayloadEntity> -> Unit },
    ),
)

@StoreProvider(id = "pendingGroupPayloads")
@CacheKey(name = "LIST", key = "pendingGroupPayloads")
fun providePendingGroupPayloadsStore(
    dao: GroupsDao,
): Store<Unit, List<GroupPayloadEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllGroupPayloads() },
        writer = { _: Unit, _: List<GroupPayloadEntity> -> Unit },
    ),
)

@StoreProvider(id = "pendingLoanRepayments")
@CacheKey(name = "LIST", key = "pendingLoanRepayments")
fun providePendingLoanRepaymentsStore(
    dao: LoanDao,
): Store<Unit, List<LoanRepaymentRequestEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.readAllLoanRepaymentTransaction() },
        writer = { _: Unit, _: List<LoanRepaymentRequestEntity> -> Unit },
    ),
)

@StoreProvider(id = "pendingSavingsTransactions")
@CacheKey(name = "LIST", key = "pendingSavingsTransactions")
fun providePendingSavingsTransactionsStore(
    dao: SavingsDao,
): Store<Unit, List<SavingsAccountTransactionRequestEntity>> = StoreFactory.createOfflineStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllSavingsAccountTransactionRequest() },
        writer = { _: Unit, _: List<SavingsAccountTransactionRequestEntity> -> Unit },
    ),
)
