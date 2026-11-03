/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.offline.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.json.JsonArray
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.data.center.impl.toCenter
import kpt.core.model.center.Center
import kpt.core.model.center.CenterAccounts
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.charge.Charges
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.datatable.DataTable
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.loan.LoanAccount
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.note.Note
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.organisations.ProductSavings
import kpt.core.model.office.Office
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.SyncTarget
import kpt.core.model.savings.SavingsAccount
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.model.shared.CollateralItemResult
import kpt.core.store.datatable.DataTableDataKey
import kpt.core.store.document.DocumentContentKey
import kpt.core.store.document.DocumentKey
import kpt.core.store.note.NoteKey
import kpt.core.store.savings.GetSavingsAccountTransactionTemplateKey
import kpt.core.store.savings.SavingsAccountKey
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/** Where the engine reports as it walks — the repository turns these into job + inventory rows. */
internal interface SyncSink {
    /** One server read finished. */
    suspend fun read(label: String)

    /** One server read was refused; the walk goes on. */
    suspend fun failed(failure: DownloadFailure)

    /** A record under the target (a centre's group, a group's client) finished — "via" its parent. */
    suspend fun record(record: OfflineRecord)
}

/**
 * One job's memory: a record reached twice (a client in two of the chosen groups, a centre's group
 * also chosen on its own) is walked ONCE, and an account read once — the second path reuses the first
 * result. Create one per job.
 */
internal class SyncRun {
    private val lock = Mutex()
    private val records = mutableMapOf<Pair<RecordKind, Int>, OfflineRecord>()
    private val accounts = mutableSetOf<String>()

    suspend fun walked(kind: RecordKind, id: Int): OfflineRecord? = lock.withLock { records[kind to id] }

    suspend fun remember(record: OfflineRecord) = lock.withLock { records[record.kind to record.id] = record }

    /** True the first time [account] is seen in this job. */
    suspend fun firstTime(account: String): Boolean = lock.withLock { accounts.add(account) }
}

/** The stores a centre / group / client screen reads, keyed exactly as the screen keys them. */
internal class EntityStores(
    val centre: Store<Int, CenterWithAssociations>,
    val centreAccounts: Store<Int, CenterAccounts>,
    val centreItem: MutableStore<Int, Center>,
    val group: Store<Int, GroupWithAssociations>,
    val groupRow: Store<Int, Group>,
    val groupAccounts: Store<Int, GroupAccounts>,
    val groupItem: MutableStore<Int, Group>,
    val client: Store<Int, Client>,
    val clientAccounts: Store<Int, ClientAccounts>,
    val addresses: Store<Int, List<ClientAddressEntity>>,
    val identifiers: Store<Long, List<Identifier>>,
    val pinpoints: Store<Int, List<ClientAddressResponse>>,
    val collateral: Store<Int, List<CollateralItemResult>>,
    val image: Store<Int, String>,
)

/** The stores shared by every owner type (notes, documents, datatables) and the global templates. */
internal class SharedStores(
    val notes: Store<NoteKey, List<Note>>,
    val documents: Store<DocumentKey, List<Document>>,
    val documentContent: Store<DocumentContentKey, ByteArray>,
    val dataTables: Store<String, List<DataTable>>,
    val dataTableRows: Store<DataTableDataKey, JsonArray>,
    val loanProducts: Store<Unit, List<LoanProducts>>,
    val savingsProducts: Store<Unit, List<ProductSavings>>,
    val offices: Store<Unit, List<Office>>,
)

/** The stores a loan / savings account screen reads. */
internal class AccountStores(
    val loan: Store<Int, LoanWithAssociations>,
    val repaymentTemplate: Store<Int, LoanRepaymentTemplate>,
    val loanCharges: Store<Int, List<Charges>>,
    val loanReschedules: Store<Int, List<LoanRescheduleResponse>>,
    val savings: Store<SavingsAccountKey, SavingsAccountWithAssociations>,
    val savingsTemplate: Store<GetSavingsAccountTransactionTemplateKey, SavingsAccountTransactionTemplate>,
)

/**
 * The download walk of Settings → Offline sync, grown out of the original `OfflineDownloadRepository`:
 *  - a centre → its record + accounts → every group,
 *  - a group → its record + accounts + notes + documents → every client,
 *  - a client → record, accounts, addresses, identifiers, pinpoints, collateral, image, notes,
 *    documents (WITH file contents) and datatable rows → every loan and savings account,
 *  - every loan (closed too) → the loan, charges, reschedules, notes, documents; its repayment
 *    template only while active,
 *  - every savings account (closed too) → the account; deposit/withdrawal templates only while active.
 *
 * Every read is `fresh` on the store, with the key, that the record's own screen reads — so "synced"
 * means that screen opens with no network, not that a parallel table was filled. A failed read is
 * reported and skipped; one refused datatable must not cost the officer the rest of the centre.
 *
 * At most [MAX_IN_FLIGHT] reads run at once (OkHttp's per-host limit), and the clients of a group are
 * walked in parallel under that cap — a centre is thousands of reads, sequential would be an hour.
 */
internal class OfflineSyncEngine(
    private val entities: EntityStores,
    private val shared: SharedStores,
    private val accounts: AccountStores,
    private val nowMs: () -> Long,
) {
    private val permits = Semaphore(MAX_IN_FLIGHT)

    /** Once per job: what the forms need to capture work offline as drafts. */
    suspend fun syncGlobals(sink: SyncSink) {
        val walk = Walk(sink, "Templates")
        walk.fetch("loan products") { shared.loanProducts.fresh(Unit) }
        walk.fetch("savings products") { shared.savingsProducts.fresh(Unit) }
        walk.fetch("offices") { shared.offices.fresh(Unit) }
    }

    /**
     * Walk one chosen record's whole tree; returns the record's own row (chosen). Pass the job's one
     * [run] to every call so records and accounts shared between targets are fetched once.
     */
    suspend fun sync(target: SyncTarget, sink: SyncSink, run: SyncRun = SyncRun()): OfflineRecord = when (target.kind) {
        RecordKind.CENTRE -> syncCentre(target, Walk(sink, target.name, run))
        RecordKind.GROUP -> syncGroup(target.id, target.name, parent = null, chosen = true, sink = sink, run = run)
        RecordKind.CLIENT -> syncClient(target.id, target.name, parent = null, chosen = true, sink = sink, run = run)
    }

    private suspend fun syncCentre(target: SyncTarget, walk: Walk): OfflineRecord {
        val sink = walk.sink
        val run = walk.run
        val centre = walk.fetch("centre") { entities.centre.fresh(target.id) }
        val own = walk.fetch("centre accounts") { entities.centreAccounts.fresh(target.id) }
            ?.let { walk.accounts(it.loanAccounts + it.memberLoanAccounts, it.savingsAccounts) } ?: Counts()
        val parent = Parent(RecordKind.CENTRE, target.id, centre?.name ?: target.name)
        val groups = centre?.groupMembers.orEmpty().mapNotNull { g -> g.id?.let { it to (g.name ?: "#$it") } }
        val children = groups.map { (id, name) -> syncGroup(id, name, parent, chosen = false, sink = sink, run = run) }
        if (centre != null) {
            walk.fetch("mark downloaded") {
                entities.centreItem.write(
                    StoreWriteRequest.of<Int, Center, Any>(
                        target.id,
                        centre.toCenter().copy(id = target.id, sync = true),
                    ),
                )
            }
        }
        return walk.row(
            Identity(RecordKind.CENTRE, target.id, parent.name, parent = null, chosen = true),
            Counts(
                groups = children.size,
                clients = children.sumOf { it.clients },
                loans = own.loans + children.sumOf { it.loans },
                savings = own.savings + children.sumOf { it.savings },
                documents = own.documents + children.sumOf { it.documents },
            ),
            childFailures = children.sumOf { it.failedCount },
        )
    }

    private suspend fun syncGroup(
        id: Int,
        name: String,
        parent: Parent?,
        chosen: Boolean,
        sink: SyncSink,
        run: SyncRun,
    ): OfflineRecord {
        run.walked(RecordKind.GROUP, id)?.let { return it.reachedAgain(parent, chosen) }
        val walk = Walk(sink, name, run)
        val group = walk.fetch("group") { entities.group.fresh(id) }
        val own = walk.fetch("group accounts") { entities.groupAccounts.fresh(id) }
            ?.let { walk.accounts(it.loanAccounts, it.savingsAccounts) } ?: Counts()
        walk.fetch("group notes") { shared.notes.fresh(NoteKey(GROUPS, id.toLong())) }
        val documents = walk.documents(GROUPS, id)
        val me = Parent(RecordKind.GROUP, id, group?.name ?: name)
        val clients = group?.clientMembers.orEmpty().mapNotNull { c -> c.id?.let { it to (c.displayName ?: "#$it") } }
        val children = coroutineScope {
            clients.map { (clientId, clientName) ->
                async { syncClient(clientId, clientName, me, chosen = false, sink = sink, run = run) }
            }
                .awaitAll()
        }
        if (chosen && group != null) {
            walk.fetch("mark downloaded") {
                val row = entities.groupRow.fresh(id)
                entities.groupItem.write(StoreWriteRequest.of<Int, Group, Any>(id, row.copy(id = id, sync = true)))
            }
        }
        val record = walk.row(
            Identity(RecordKind.GROUP, id, me.name, parent, chosen),
            Counts(
                clients = children.size,
                loans = own.loans + children.sumOf { it.loans },
                savings = own.savings + children.sumOf { it.savings },
                documents = own.documents + documents + children.sumOf { it.documents },
            ),
            childFailures = children.sumOf { it.failedCount },
        )
        run.remember(record)
        if (!chosen) sink.record(record)
        return record
    }

    private suspend fun syncClient(
        id: Int,
        name: String,
        parent: Parent?,
        chosen: Boolean,
        sink: SyncSink,
        run: SyncRun,
    ): OfflineRecord {
        run.walked(RecordKind.CLIENT, id)?.let { return it.reachedAgain(parent, chosen) }
        val walk = Walk(sink, name, run)
        val client = walk.fetch("client") { entities.client.fresh(id) }
        val own = walk.fetch("accounts") { entities.clientAccounts.fresh(id) }
            ?.let { walk.accounts(it.loanAccounts, it.savingsAccounts) } ?: Counts()
        var documents = 0
        if (client != null) {
            walk.fetch("addresses") { entities.addresses.fresh(id) }
            walk.fetch("identifiers") { entities.identifiers.fresh(id.toLong()) }
            walk.fetch("pinpoint locations") { entities.pinpoints.fresh(id) }
            walk.fetch("collateral") { entities.collateral.fresh(id) }
            // A client with no photo answers 404 — that is "no image", not a failure.
            walk.optional { entities.image.fresh(id) }
            walk.fetch("notes") { shared.notes.fresh(NoteKey(CLIENTS, id.toLong())) }
            walk.dataTables(CLIENT_TABLE, id)
            documents = walk.documents(CLIENTS, id)
        }
        val record = walk.row(
            Identity(RecordKind.CLIENT, id, client?.displayName ?: name, parent, chosen),
            own.copy(documents = own.documents + documents),
        )
        run.remember(record)
        if (!chosen) sink.record(record)
        return record
    }

    /** The first path's result, as seen from the second: same counts, the second path's role. */
    private fun OfflineRecord.reachedAgain(parent: Parent?, chosen: Boolean): OfflineRecord =
        if (chosen) {
            copy(chosen = true)
        } else {
            copy(
                parentKind = parent?.kind,
                parentId = parent?.id,
                parentName = parent?.name,
                chosen = false,
            )
        }

    /** Every account, closed included (its history is part of the record). Templates only while active. */
    private suspend fun Walk.accounts(loans: List<LoanAccount>, savings: List<SavingsAccount>): Counts {
        var documents = 0
        for (loan in loans) documents += loanRecord(loan)
        for (account in savings) savingsRecord(account)
        return Counts(
            loans = loans.count { it.id != null },
            savings = savings.count { it.id != null },
            documents = documents,
        )
    }

    /** One loan's reads; returns its document count (0 when it has no id or was already walked). */
    private suspend fun Walk.loanRecord(loan: LoanAccount): Int {
        val id = loan.id
        if (id == null || !run.firstTime("loan:$id")) return 0
        fetch("loan #$id") { accounts.loan.fresh(id) }
        fetch("loan #$id charges") { accounts.loanCharges.fresh(id) }
        fetch("loan #$id reschedules") { accounts.loanReschedules.fresh(id) }
        fetch("loan #$id notes") { shared.notes.fresh(NoteKey(LOANS, id.toLong())) }
        val documents = documents(LOANS, id)
        if (loan.status?.active == true) {
            fetch("loan #$id repayment template") { accounts.repaymentTemplate.fresh(id) }
        }
        return documents
    }

    private suspend fun Walk.savingsRecord(account: SavingsAccount) {
        val id = account.id ?: return
        val type = account.depositType?.endpoint ?: SAVINGS_ENDPOINT
        if (!run.firstTime("$type:$id")) return
        fetch("savings #$id") { accounts.savings.fresh(SavingsAccountKey(type, id)) }
        // Only an active account takes deposits / withdrawals, so only it needs the templates.
        val commands = if (account.status?.active == true) TRANSACTION_COMMANDS else emptyList()
        for (command in commands) {
            fetch("savings #$id $command template") {
                accounts.savingsTemplate.fresh(GetSavingsAccountTransactionTemplateKey(type, id, command))
            }
        }
    }

    /** The document list AND each file's contents — the officer opens them in the field. */
    private suspend fun Walk.documents(entityType: String, entityId: Int): Int {
        val list = fetch("documents") { shared.documents.fresh(DocumentKey(entityType, entityId)) } ?: return 0
        for (document in list) {
            fetch("document “${document.fileName ?: document.name ?: document.id}”") {
                shared.documentContent.fresh(DocumentContentKey(entityType, entityId, document.id))
            }
        }
        return list.size
    }

    private suspend fun Walk.dataTables(appTable: String, entityId: Int) {
        val tables = fetch("datatables") { shared.dataTables.fresh(appTable) } ?: return
        for (table in tables) {
            val tableName = table.registeredTableName ?: continue
            fetch(tableName) { shared.dataTableRows.fresh(DataTableDataKey(tableName, entityId)) }
        }
    }

    private data class Parent(val kind: RecordKind, val id: Int, val name: String)

    private data class Identity(
        val kind: RecordKind,
        val id: Int,
        val name: String,
        val parent: Parent?,
        val chosen: Boolean,
    )

    private data class Counts(
        val groups: Int = 0,
        val clients: Int = 0,
        val loans: Int = 0,
        val savings: Int = 0,
        val documents: Int = 0,
    )

    /**
     * One record's walk: reads run under the shared permit cap; failures are counted for THIS record
     * (a record's own reads are sequential, so the counter is never shared between coroutines).
     */
    private inner class Walk(val sink: SyncSink, private val label: String, val run: SyncRun = SyncRun()) {
        private var failures = 0

        suspend fun <T : Any> fetch(what: String, block: suspend () -> T): T? = permits.withPermit {
            try {
                block().also { sink.read("$label · $what") }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                failures++
                sink.failed(DownloadFailure("$label · $what", error.serverErrorMessage() ?: error.statusReason()))
                null
            }
        }

        /** A read whose absence is a valid answer (a client's photo) — never counted as failed. */
        suspend fun optional(block: suspend () -> Any) {
            permits.withPermit { runCatchingNonCancellation(block) }
        }

        fun row(who: Identity, counts: Counts, childFailures: Int = 0): OfflineRecord {
            val failed = failures + childFailures
            return OfflineRecord(
                kind = who.kind,
                id = who.id,
                name = who.name,
                parentKind = who.parent?.kind,
                parentId = who.parent?.id,
                parentName = who.parent?.name,
                chosen = who.chosen,
                groups = counts.groups,
                clients = counts.clients,
                loans = counts.loans,
                savings = counts.savings,
                documents = counts.documents,
                status = if (failed == 0) RecordSyncStatus.SYNCED else RecordSyncStatus.FAILED,
                refreshedAtMs = nowMs(),
                failedCount = failed,
            )
        }
    }

    companion object {
        /** OkHttp's default per-host limit — more in flight only queues on the client. */
        const val MAX_IN_FLIGHT = 5

        private const val CLIENTS = "clients"
        private const val GROUPS = "groups"
        private const val LOANS = "loans"
        private const val CLIENT_TABLE = "m_client"

        /** `ServerTypes.SAVINGS.endpoint` — an account that does not say otherwise is a plain savings account. */
        private const val SAVINGS_ENDPOINT = "savingsaccounts"

        /** The commands the savings transaction screen asks a template for (`TransactionKind`). */
        private val TRANSACTION_COMMANDS = listOf("deposit", "withdrawal")
    }
}

/**
 * The HTTP status when the body names no reason — a gateway 500 for a file missing from its storage
 * answers `{"error":"Internal Server Error"}` with no Fineract message, and a blank row tells the
 * officer nothing. The status code is a technical fact, not copy, so it is not localised.
 */
internal fun Throwable.statusReason(): String? = when (val category = categorize(this)) {
    is ErrorCategory.ClientError -> "HTTP ${category.httpCode}"
    is ErrorCategory.Server -> "HTTP ${category.httpCode ?: SERVER_ERROR}"
    else -> null
}

private const val SERVER_ERROR = 500

/** Runs [block], letting cancellation through and treating any other failure as an absent answer. */
private suspend fun runCatchingNonCancellation(block: suspend () -> Any) {
    try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        // absent is a valid answer for an optional read
    }
}
