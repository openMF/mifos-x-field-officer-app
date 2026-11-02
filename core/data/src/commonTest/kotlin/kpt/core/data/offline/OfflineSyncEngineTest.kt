/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.offline

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kpt.core.base.store.infra.StoreFactory
import kpt.core.data.offline.impl.AccountStores
import kpt.core.data.offline.impl.EntityStores
import kpt.core.data.offline.impl.OfflineSyncEngine
import kpt.core.data.offline.impl.SharedStores
import kpt.core.data.offline.impl.SyncRun
import kpt.core.data.offline.impl.SyncSink
import kpt.core.data.offline.impl.decodeFailures
import kpt.core.data.offline.impl.decodeTargets
import kpt.core.data.offline.impl.encodeFailures
import kpt.core.data.offline.impl.encodeTargets
import kpt.core.model.center.Center
import kpt.core.model.center.CenterAccounts
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.datatable.DataTable
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.loan.LoanAccount
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanStatus
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.SyncTarget
import kpt.core.model.savings.SavingAccountDepositType
import kpt.core.model.savings.SavingsAccount
import kpt.core.model.savings.SavingsAccountStatus
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.store.document.DocumentKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineSyncEngineTest {

    /** Every network read — "what did the sync fetch". */
    private val fetched = mutableListOf<String>()

    /** A read-only store answering from [answer]; a null answer is a server error. */
    private fun <K : Any, V : Any> store(name: String, answer: (K) -> V?): Store<K, V> = StoreBuilder.from(
        fetcher = Fetcher.of { key: K ->
            fetched += "$name:$key"
            answer(key) ?: throw RuntimeException("$name $key not found")
        },
    ).build()

    private val centerRows = MutableStateFlow<Map<Int, Center>>(emptyMap())
    private val groupRows = MutableStateFlow<Map<Int, Group>>(emptyMap())

    private val activeLoan = LoanAccount(id = 501, status = LoanStatus(active = true))
    private val closedLoan = LoanAccount(id = 502, status = LoanStatus(active = false))
    private val fixedDeposit = SavingsAccount(
        id = 601,
        status = SavingsAccountStatus(active = true),
        depositType = SavingAccountDepositType(id = 200),
    )

    private val engine = OfflineSyncEngine(
        entities = EntityStores(
            centre = store("centre") { id: Int ->
                if (id == 1) {
                    CenterWithAssociations(
                        id = 1,
                        name = "North",
                        groupMembers = listOf(Group(id = 10, name = "Weavers")),
                    )
                } else {
                    null
                }
            },
            centreAccounts = store("centreAccounts") { _: Int -> CenterAccounts() },
            centreItem = StoreFactory.createOfflineMutableStore(
                sourceOfTruth = SourceOfTruth.of(
                    reader = { id: Int -> centerRows.map { it[id] } },
                    writer = { id: Int, row: Center -> centerRows.value += id to row },
                ),
            ),
            // Group 10 has two clients; client 101's detail is refused by the server.
            group = store("group") { id: Int ->
                if (id == 10) {
                    GroupWithAssociations(
                        id = 10,
                        name = "Weavers",
                        clientMembers = listOf(
                            Client(id = 100, displayName = "Asha"),
                            Client(id = 101, displayName = "Bimal"),
                        ),
                    )
                } else {
                    null
                }
            },
            groupRow = store("groupRow") { id: Int -> Group(id = id, name = "Weavers") },
            groupAccounts = store("groupAccounts") { _: Int -> GroupAccounts() },
            groupItem = StoreFactory.createOfflineMutableStore(
                sourceOfTruth = SourceOfTruth.of(
                    reader = { id: Int -> groupRows.map { it[id] } },
                    writer = { id: Int, row: Group -> groupRows.value += id to row },
                ),
            ),
            client = store("client") { id: Int -> if (id == 100) Client(id = 100, displayName = "Asha") else null },
            clientAccounts = store("clientAccounts") { _: Int ->
                ClientAccounts(
                    loanAccounts = listOf(activeLoan, closedLoan),
                    savingsAccounts = listOf(fixedDeposit),
                )
            },
            addresses = store("addresses") { _: Int -> emptyList() },
            identifiers = store("identifiers") { _: Long -> emptyList() },
            pinpoints = store("pinpoints") { _: Int -> emptyList() },
            collateral = store("collateral") { _: Int -> emptyList() },
            // no photo: 404
            image = store("image") { _: Int -> null },
        ),
        shared = SharedStores(
            notes = store("notes") { _ -> emptyList() },
            documents = store("documents") { key: DocumentKey ->
                if (key == DocumentKey("clients", 100)) listOf(Document(id = 7, fileName = "id.jpg")) else emptyList()
            },
            documentContent = store("documentContent") { _ -> ByteArray(1) },
            dataTables = store("dataTables") { _: String -> listOf(DataTable(registeredTableName = "DATOS_CONTACTO")) },
            dataTableRows = store("dataTableRows") { _ -> JsonArray(emptyList()) },
            loanProducts = store("loanProducts") { _: Unit -> emptyList() },
            savingsProducts = store("savingsProducts") { _: Unit -> emptyList() },
            offices = store("offices") { _: Unit -> emptyList() },
        ),
        accounts = AccountStores(
            loan = store("loan") { _: Int -> LoanWithAssociations() },
            repaymentTemplate = store("repaymentTemplate") { _: Int -> LoanRepaymentTemplate() },
            loanCharges = store("loanCharges") { _: Int -> emptyList() },
            loanReschedules = store("loanReschedules") { _: Int -> emptyList() },
            savings = store("savings") { _ -> SavingsAccountWithAssociations() },
            savingsTemplate = store("savingsTemplate") { _ -> SavingsAccountTransactionTemplate() },
        ),
        nowMs = { NOW },
    )

    private val reads = mutableListOf<String>()
    private val failures = mutableListOf<DownloadFailure>()
    private val children = mutableListOf<OfflineRecord>()

    private val sink = object : SyncSink {
        override suspend fun read(label: String) {
            reads += label
        }

        override suspend fun failed(failure: DownloadFailure) {
            failures += failure
        }

        override suspend fun record(record: OfflineRecord) {
            children += record
        }
    }

    @Test
    fun aCentreIsWalkedDownToEveryAccountIncludingClosedOnes() = runTest {
        val centre = engine.sync(SyncTarget(RecordKind.CENTRE, 1, "North"), sink)

        // Closed accounts come down too — their history is part of the record.
        assertTrue("loan:501" in fetched && "loan:502" in fetched)
        // …but only an ACTIVE loan gets a repayment template (nothing can be collected on a closed one).
        assertTrue("repaymentTemplate:501" in fetched)
        assertTrue("repaymentTemplate:502" !in fetched)
        // A fixed deposit is read under ITS endpoint — the key the savings summary streams.
        assertTrue(fetched.any { it.startsWith("savings:") && "fixeddepositaccounts" in it })
        assertEquals(2, fetched.count { it.startsWith("savingsTemplate:") }) // deposit + withdrawal
        // Documents come down WITH their file contents; datatable rows for the client.
        assertTrue(fetched.any { it.startsWith("documentContent:") && "clients" in it && "7" in it })
        assertTrue(fetched.any { it.startsWith("dataTableRows:") && "DATOS_CONTACTO" in it })

        assertEquals(1, centre.groups)
        assertEquals(2, centre.clients)
        assertTrue(centre.chosen)
        // The selected centre is marked downloaded — what its list shows.
        assertEquals(true, centerRows.value[1]?.sync)
    }

    @Test
    fun childrenAreReportedViaTheirParentAndFailuresStayWithTheRecordThatHadThem() = runTest {
        val centre = engine.sync(SyncTarget(RecordKind.CENTRE, 1, "North"), sink)

        val group = children.single { it.kind == RecordKind.GROUP }
        assertEquals(RecordKind.CENTRE, group.parentKind)
        assertEquals("North", group.parentName)
        val asha = children.single { it.kind == RecordKind.CLIENT && it.id == 100 }
        val bimal = children.single { it.kind == RecordKind.CLIENT && it.id == 101 }
        assertEquals("Weavers", asha.parentName)
        assertEquals(RecordSyncStatus.SYNCED, asha.status)
        assertEquals(1, asha.documents)
        // Bimal's detail was refused: HIS record failed, and the failure carries his name.
        assertEquals(RecordSyncStatus.FAILED, bimal.status)
        assertTrue(failures.any { "Bimal" in it.item })
        // The centre rolls up its children's failures, so its row says "1 failed".
        assertEquals(RecordSyncStatus.FAILED, centre.status)
        assertEquals(1, centre.failedCount)
        assertEquals(NOW, centre.refreshedAtMs)
    }

    @Test
    fun aRecordReachedTwiceInOneJobIsFetchedOnce() = runTest {
        val run = SyncRun()
        engine.sync(SyncTarget(RecordKind.CENTRE, 1, "North"), sink, run)
        val before = fetched.size
        // Weavers is the centre's group AND chosen on its own: the second path reuses the first walk.
        val weavers = engine.sync(SyncTarget(RecordKind.GROUP, 10, "Weavers"), sink, run)

        assertEquals(before, fetched.size, "nothing is read twice in one job")
        assertTrue(weavers.chosen)
        assertEquals(2, weavers.clients)
        // The same savings account under two clients is read once.
        assertEquals(1, fetched.count { it.startsWith("savings:") })
    }

    @Test
    fun aMissingPhotoIsNotAFailure() = runTest {
        val asha = engine.sync(SyncTarget(RecordKind.CLIENT, 100, "Asha"), sink)

        assertTrue("image:100" in fetched)
        assertEquals(RecordSyncStatus.SYNCED, asha.status)
        assertTrue(failures.isEmpty())
    }

    @Test
    fun aSelectedGroupIsMarkedButACentresGroupIsNot() = runTest {
        engine.sync(SyncTarget(RecordKind.CENTRE, 1, "North"), sink)
        assertTrue(groupRows.value.isEmpty(), "a centre's groups ride on the centre's flag")

        engine.sync(SyncTarget(RecordKind.GROUP, 10, "Weavers"), sink)
        assertEquals(true, groupRows.value[10]?.sync)
    }

    @Test
    fun jobColumnsRoundTripEvenWithTabsAndNewlinesInNames() {
        val targets = listOf(
            SyncTarget(RecordKind.CENTRE, 1, "North\tside"),
            SyncTarget(RecordKind.CLIENT, 9, "Ana\nRíos"),
        )
        assertEquals(
            listOf(SyncTarget(RecordKind.CENTRE, 1, "North side"), SyncTarget(RecordKind.CLIENT, 9, "Ana Ríos")),
            targets.encodeTargets().decodeTargets(),
        )

        val failures = listOf(
            DownloadFailure("Asha · notes", "Access denied"),
            DownloadFailure("Bimal · client", null),
        )
        assertEquals(failures, failures.encodeFailures().decodeFailures())
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
