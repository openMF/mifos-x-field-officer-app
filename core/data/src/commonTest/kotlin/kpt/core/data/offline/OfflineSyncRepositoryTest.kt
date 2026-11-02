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

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.store.infra.StoreFactory
import kpt.core.data.offline.impl.AccountStores
import kpt.core.data.offline.impl.EntityStores
import kpt.core.data.offline.impl.OfflineSyncEngine
import kpt.core.data.offline.impl.OfflineSyncRepositoryImpl
import kpt.core.data.offline.impl.SharedStores
import kpt.core.database.offline.dao.OfflineSyncDao
import kpt.core.database.offline.entity.OfflineRecordEntity
import kpt.core.database.offline.entity.SyncJobEntity
import kpt.core.datastore.offline.SettingsOfflineSyncSettingsStore
import kpt.core.datastore.prefs.ForkPreferencesRepositoryImpl
import kpt.core.datastore.prefs.UserPreferencesRepositoryImpl
import kpt.core.model.center.Center
import kpt.core.model.center.CenterAccounts
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.users.User
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.SavingsAccountWithAssociations
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OfflineSyncRepositoryTest {

    private val dispatchers = object : DispatcherManager {
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: MainCoroutineDispatcher = Dispatchers.Main
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        override val appScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }

    private val plain = MapSettings()
    private val preferences = ForkPreferencesRepositoryImpl(
        delegate = UserPreferencesRepositoryImpl(plain, MapSettings(), dispatchers),
        plainSettings = plain,
        secureSettings = MapSettings(),
        dispatcher = dispatchers,
    )

    /** Client 101 is refused by the server — the one failure in Centre 1's tree. */
    private var refuseClient101 = true

    private fun <K : Any, V : Any> store(answer: (K) -> V?): Store<K, V> = StoreBuilder.from(
        fetcher = Fetcher.of { key: K -> answer(key) ?: throw RuntimeException("not found") },
    ).build()

    private fun <V : Any> mutableRows() = MutableStateFlow<Map<Int, V>>(emptyMap()).let { rows ->
        StoreFactory.createOfflineMutableStore(
            sourceOfTruth = SourceOfTruth.of(
                reader = { id: Int -> rows.map { it[id] } },
                writer = { id: Int, row: V -> rows.value += id to row },
            ),
        )
    }

    private val engine = OfflineSyncEngine(
        entities = EntityStores(
            centre = store { _: Int ->
                CenterWithAssociations(
                    id = 1,
                    name = "North",
                    groupMembers = listOf(Group(id = 10, name = "Weavers")),
                )
            },
            centreAccounts = store { _: Int -> CenterAccounts() },
            centreItem = mutableRows<Center>(),
            group = store { _: Int ->
                GroupWithAssociations(
                    id = 10,
                    name = "Weavers",
                    clientMembers = listOf(
                        Client(id = 100, displayName = "Asha"),
                        Client(id = 101, displayName = "Bimal"),
                    ),
                )
            },
            groupRow = store { id: Int -> Group(id = id) },
            groupAccounts = store { _: Int -> GroupAccounts() },
            groupItem = mutableRows<Group>(),
            client = store { id: Int -> if (id == 101 && refuseClient101) null else Client(id = id) },
            clientAccounts = store { _: Int -> ClientAccounts() },
            addresses = store { _: Int -> emptyList() },
            identifiers = store { _: Long -> emptyList() },
            pinpoints = store { _: Int -> emptyList() },
            collateral = store { _: Int -> emptyList() },
            image = store { _: Int -> "" },
        ),
        shared = SharedStores(
            notes = store { _ -> emptyList() },
            documents = store { _ -> emptyList() },
            documentContent = store { _ -> ByteArray(0) },
            dataTables = store { _: String -> emptyList() },
            dataTableRows = store { _ -> JsonArray(emptyList()) },
            loanProducts = store { _: Unit -> emptyList() },
            savingsProducts = store { _: Unit -> emptyList() },
            offices = store { _: Unit -> emptyList() },
        ),
        accounts = AccountStores(
            loan = store { _: Int -> LoanWithAssociations() },
            repaymentTemplate = store { _: Int -> LoanRepaymentTemplate() },
            loanCharges = store { _: Int -> emptyList() },
            loanReschedules = store { _: Int -> emptyList() },
            savings = store { _ -> SavingsAccountWithAssociations() },
            savingsTemplate = store { _ -> SavingsAccountTransactionTemplate() },
        ),
        nowMs = { NOW },
    )

    private val dao = InMemoryOfflineSyncDao()
    private val scheduler = RecordingScheduler()
    private val repository = OfflineSyncRepositoryImpl(
        dao = dao,
        engine = engine,
        scheduler = scheduler,
        settingsStore = SettingsOfflineSyncSettingsStore(MapSettings()),
        preferences = preferences,
        nowMs = { NOW },
    )

    private suspend fun signIn() = preferences.updateFineractUser(User(userId = USER))

    @Test
    fun downloadingAChosenCentreSyncsItsWholeTreeInTheBackground() = runTest {
        signIn()
        repository.download(listOf(SyncTarget(RecordKind.CENTRE, 1, "North")))

        // Enqueued, not run in the caller — the work belongs to the background job.
        assertEquals(listOf("runNow"), scheduler.calls)
        assertEquals(RecordSyncStatus.WAITING, repository.records.first().single().status)

        repository.runPending(refreshAll = false)

        val records = repository.records.first().associateBy { it.kind to it.id }
        val centre = records.getValue(RecordKind.CENTRE to 1)
        assertTrue(centre.chosen)
        assertEquals(2, centre.clients)
        // The group and its clients are on the device too, "via" their parent — not chosen.
        val weavers = records.getValue(RecordKind.GROUP to 10)
        assertEquals("North", weavers.parentName)
        assertEquals(false, weavers.chosen)
        assertEquals(RecordSyncStatus.SYNCED, records.getValue(RecordKind.CLIENT to 100).status)
        // Bimal was refused: his row and the centre's say so, with the reason on the job.
        assertEquals(RecordSyncStatus.FAILED, records.getValue(RecordKind.CLIENT to 101).status)
        assertEquals(RecordSyncStatus.FAILED, centre.status)
        val job = repository.latestJob.first()!!
        assertEquals(SyncJobStatus.FINISHED, job.status)
        assertEquals(1, job.done)
        assertTrue(job.reads > 0)
        assertTrue(job.failures.any { "Bimal" in it.item })
    }

    @Test
    fun retryFailedReRunsOnlyTheRecordsThatFailed() = runTest {
        signIn()
        repository.download(
            listOf(
                SyncTarget(RecordKind.CENTRE, 1, "North"),
                SyncTarget(RecordKind.CLIENT, 100, "Asha"),
            ),
        )
        repository.runPending(refreshAll = false)

        refuseClient101 = false
        repository.retryFailed()
        val waiting = repository.records.first().filter { it.chosen && it.status == RecordSyncStatus.WAITING }
        assertEquals(
            listOf(RecordKind.CENTRE to 1),
            waiting.map { it.kind to it.id },
            "Asha synced fine — only the centre re-runs",
        )

        repository.runPending(refreshAll = false)
        assertEquals(RecordSyncStatus.SYNCED, repository.records.first().single { it.kind == RecordKind.CENTRE }.status)
    }

    @Test
    fun aRecordChosenAndAlsoUnderAChosenCentreStaysChosen() = runTest {
        signIn()
        repository.download(
            listOf(
                SyncTarget(RecordKind.CLIENT, 100, "Asha"),
                SyncTarget(RecordKind.CENTRE, 1, "North"),
            ),
        )
        repository.runPending(refreshAll = false)

        val asha = repository.records.first().single { it.kind == RecordKind.CLIENT && it.id == 100 }
        assertTrue(asha.chosen, "reaching her through the centre must not un-choose her")
    }

    @Test
    fun cancellingPutsUnfinishedRecordsBackToWaiting() = runTest {
        signIn()
        repository.download(listOf(SyncTarget(RecordKind.CENTRE, 1, "North")))
        dao.insertJob(
            SyncJobEntity(
                userId = USER,
                status = "RUNNING",
                targets = "",
                done = 0,
                total = 1,
                reads = 3,
                currentLabel = "x",
                failures = "",
                startedAtMs = NOW,
                finishedAtMs = null,
            ),
        )
        dao.setStatus(USER, "CENTRE", 1, "SYNCING")

        repository.cancel()

        assertTrue("cancelRun" in scheduler.calls)
        assertEquals(SyncJobStatus.CANCELLED, repository.latestJob.first()!!.status)
        assertEquals(RecordSyncStatus.WAITING, repository.records.first().single().status)
    }

    @Test
    fun aRunKilledMidJobIsResumedWhenTheWorkRunsAgain() = runTest {
        signIn()
        repository.download(listOf(SyncTarget(RecordKind.CENTRE, 1, "North")))
        // The process died mid-job: its row says RUNNING and the centre says SYNCING.
        dao.insertJob(
            SyncJobEntity(
                userId = USER,
                status = "RUNNING",
                targets = "",
                done = 0,
                total = 1,
                reads = 40,
                currentLabel = "x",
                failures = "",
                startedAtMs = NOW,
                finishedAtMs = null,
            ),
        )
        dao.setStatus(USER, "CENTRE", 1, "SYNCING")

        repository.runPending(refreshAll = false)

        assertEquals(
            RecordSyncStatus.FAILED,
            repository.records.first().single { it.kind == RecordKind.CENTRE }.status,
            "re-walked (Bimal still refused)",
        )
        assertEquals(SyncJobStatus.FINISHED, repository.latestJob.first()!!.status)
    }

    @Test
    fun removingACentreRemovesWhatCameInOnlyThroughIt() = runTest {
        signIn()
        repository.download(
            listOf(
                SyncTarget(RecordKind.CENTRE, 1, "North"),
                SyncTarget(RecordKind.CLIENT, 100, "Asha"),
            ),
        )
        repository.runPending(refreshAll = false)

        repository.remove(SyncTarget(RecordKind.CENTRE, 1, "North"))

        val left = repository.records.first().map { it.kind to it.id }
        assertEquals(listOf(RecordKind.CLIENT to 100), left, "Asha was chosen on her own — she stays")
    }

    @Test
    fun automaticSchedulesThePeriodicJobAndManualCancelsIt() = runTest {
        signIn()
        val automatic = RefreshSettings(mode = RefreshMode.AUTOMATIC, intervalHours = 12, wifiOnly = true)
        repository.setRefresh(automatic)
        assertEquals(automatic, repository.refresh.value)

        repository.setRefresh(automatic.copy(mode = RefreshMode.MANUAL))
        assertEquals(listOf("schedule:AUTOMATIC:12", "schedule:MANUAL:12"), scheduler.calls)
    }

    @Test
    fun thePeriodicRunReSyncsEveryChosenRecord() = runTest {
        signIn()
        repository.download(listOf(SyncTarget(RecordKind.CLIENT, 100, "Asha")))
        repository.runPending(refreshAll = false)
        val first = repository.latestJob.first()!!.id

        repository.runPending(refreshAll = true)

        assertTrue(repository.latestJob.first()!!.id > first, "a new job ran for the already-synced client")
    }

    private class RecordingScheduler : OfflineSyncScheduler {
        val calls = mutableListOf<String>()
        override suspend fun runNow() {
            calls += "runNow"
        }

        override suspend fun cancelRun() {
            calls += "cancelRun"
        }

        override suspend fun schedule(settings: RefreshSettings) {
            calls += "schedule:${settings.mode}:${settings.intervalHours}"
        }
    }

    private companion object {
        const val USER = 32L
        const val NOW = 1_700_000_000_000L
    }
}

/** Room's contract for the two tables, in memory. */
private class InMemoryOfflineSyncDao : OfflineSyncDao {
    private val records = MutableStateFlow<Map<Triple<Long, String, Int>, OfflineRecordEntity>>(emptyMap())
    private val jobs = MutableStateFlow<Map<Long, SyncJobEntity>>(emptyMap())
    private var nextId = 1L

    override fun observeRecords(userId: Long): Flow<List<OfflineRecordEntity>> =
        records.map { all -> all.values.filter { it.userId == userId }.sortedBy { it.name.lowercase() } }

    override suspend fun records(userId: Long) = records.value.values.filter { it.userId == userId }

    override suspend fun record(userId: Long, kind: String, id: Int) = records.value[Triple(userId, kind, id)]

    override suspend fun upsertRecord(record: OfflineRecordEntity) {
        records.value += Triple(record.userId, record.kind, record.id) to record
    }

    override suspend fun setStatus(userId: Long, kind: String, id: Int, status: String) {
        record(userId, kind, id)?.let { upsertRecord(it.copy(status = status)) }
    }

    override suspend fun moveStatus(userId: Long, from: String, to: String) {
        records(userId).filter { it.status == from }.forEach { upsertRecord(it.copy(status = to)) }
    }

    override suspend fun deleteRecord(userId: Long, kind: String, id: Int) {
        records.value -= Triple(userId, kind, id)
    }

    override suspend fun deleteChildren(userId: Long, parentKind: String, parentId: Int) {
        records.value = records.value.filterValues {
            !(it.userId == userId && it.parentKind == parentKind && it.parentId == parentId && !it.chosen)
        }
    }

    override suspend fun children(userId: Long, parentKind: String, parentId: Int) =
        records(userId).filter { it.parentKind == parentKind && it.parentId == parentId }

    override fun observeLatestJob(userId: Long): Flow<SyncJobEntity?> =
        jobs.map { all -> all.values.filter { it.userId == userId }.maxByOrNull { it.id } }

    override suspend fun runningJob(
        userId: Long,
    ) = jobs.value.values.filter { it.userId == userId && it.status == "RUNNING" }.maxByOrNull { it.id }

    override suspend fun insertJob(job: SyncJobEntity): Long {
        val id = if (job.id == 0L) nextId++ else job.id
        jobs.value += id to job.copy(id = id)
        return id
    }

    override suspend fun updateJob(job: SyncJobEntity) {
        jobs.value += job.id to job
    }

    override suspend fun trimJobs(userId: Long, keep: Int) {
        val keepIds = jobs.value.values.filter { it.userId == userId }
            .sortedByDescending { it.id }
            .take(keep)
            .map { it.id }
            .toSet()
        jobs.value = jobs.value.filterValues { it.userId != userId || it.id in keepIds }
    }
}
