/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.offlinesync.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.offline.DownloadFailure
import kpt.core.model.offline.OfflineRecord
import kpt.core.model.offline.RecordKind
import kpt.core.model.offline.RecordSyncStatus
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings
import kpt.core.model.offline.SyncJob
import kpt.core.model.offline.SyncJobStatus
import kpt.core.model.offline.SyncTarget
import org.jetbrains.compose.ui.tooling.preview.Preview
import kpt.core.base.designsystem.theme.KptTheme as KptBaseTheme

// Demo tenant names (mifos-bank-1) — the same inventory the idea-layer demo-data and Stitch renders use.
private const val NOW = 1_800_000_000_000L
private const val HOUR = 3_600_000L

private val centroArin = OfflineRecord(
    RecordKind.CENTRE,
    10,
    "Centro Arin",
    chosen = true,
    groups = 3,
    clients = 31,
    loans = 52,
    savings = 22,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 2 * HOUR,
)
private val arinSavings = OfflineRecord(
    RecordKind.CENTRE,
    18,
    "Arin Savings",
    chosen = true,
    groups = 2,
    clients = 11,
    status = RecordSyncStatus.WAITING,
)
private val mariposas = OfflineRecord(
    RecordKind.GROUP,
    84,
    "MARIPOSAS MONARCAS",
    parentKind = RecordKind.CENTRE,
    parentId = 10,
    parentName = "Centro Arin",
    clients = 12,
    loans = 18,
    savings = 9,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 2 * HOUR,
)
private val margaritas = OfflineRecord(
    RecordKind.GROUP,
    85,
    "Las Margaritas",
    parentKind = RecordKind.CENTRE,
    parentId = 10,
    parentName = "Centro Arin",
    clients = 10,
    loans = 15,
    savings = 7,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 2 * HOUR,
)
private val girasoles = OfflineRecord(
    RecordKind.GROUP,
    86,
    "Girasoles",
    parentKind = RecordKind.CENTRE,
    parentId = 10,
    parentName = "Centro Arin",
    clients = 9,
    loans = 19,
    savings = 6,
    status = RecordSyncStatus.FAILED,
    refreshedAtMs = NOW - 2 * HOUR,
    failedCount = 2,
)
private val asha = OfflineRecord(
    RecordKind.CLIENT,
    192,
    "FOTest Asha",
    parentKind = RecordKind.GROUP,
    parentId = 84,
    parentName = "MARIPOSAS MONARCAS",
    loans = 2,
    savings = 1,
    documents = 3,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 2 * HOUR,
)
private val maria = OfflineRecord(
    RecordKind.CLIENT,
    310,
    "María Guadalupe Torres",
    parentKind = RecordKind.GROUP,
    parentId = 84,
    parentName = "MARIPOSAS MONARCAS",
    loans = 1,
    savings = 2,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 2 * HOUR,
)
private val irving = OfflineRecord(
    RecordKind.CLIENT,
    205,
    "Irving Zamora",
    chosen = true,
    loans = 1,
    savings = 1,
    status = RecordSyncStatus.SYNCED,
    refreshedAtMs = NOW - 50 * HOUR,
)

private val inventory = listOf(centroArin, arinSavings, mariposas, margaritas, girasoles, asha, maria, irving)

private val base = OfflineSyncState(records = inventory, nowMs = NOW, expanded = setOf(RecordKind.CENTRE to 10))

@Composable
private fun Render(
    state: OfflineSyncState,
    picker: ScreenState<List<PickerRow>> = ScreenState.Loading,
) = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.background) {
        OfflineSyncScreenContent(state = state.copy(picker = null), pickerRows = picker, onAction = {})
    }
}

@Preview @Composable
internal fun OfflineSyncLoadingPreview() = Render(OfflineSyncState(nowMs = NOW))

@Preview @Composable
internal fun OfflineSyncContentPreview() = Render(base)

@Preview @Composable
internal fun OfflineSyncSyncingPreview() = Render(
    base.copy(
        records = inventory.map { if (it.id == 18) it.copy(status = RecordSyncStatus.SYNCING) else it },
        job = SyncJob(
            id = 7,
            status = SyncJobStatus.RUNNING,
            targets = listOf(
                SyncTarget(RecordKind.CENTRE, 18, "Arin Savings"),
                SyncTarget(RecordKind.CLIENT, 205, "Irving Zamora"),
            ),
            // done = 1: a determinate bar (done = 0 animates indeterminately, which no golden can pin).
            done = 1,
            reads = 312,
            currentLabel = "Las Palomas · client loans",
            startedAtMs = NOW,
        ),
        refresh = RefreshSettings(mode = RefreshMode.AUTOMATIC, intervalHours = 6, wifiOnly = true),
    ),
)

@Preview @Composable
internal fun OfflineSyncPartialFailurePreview() = Render(
    base.copy(
        level = RecordKind.GROUP,
        job = SyncJob(
            id = 6,
            status = SyncJobStatus.FINISHED,
            targets = listOf(centroArin.target),
            done = 1,
            reads = 1037,
            failures = listOf(
                DownloadFailure(
                    "Loan 000001475 · documents",
                    "Access denied (403): User has no authority to: READ_DOCUMENT",
                ),
                DownloadFailure(
                    "FOTest Asha · DATOS_CONTACTO",
                    "Access denied (403): Not authorised to read datatable: DATOS_CONTACTO",
                ),
                DownloadFailure(
                    "Rosa Elena Cruz · document “INE frente.jpg”",
                    "Not found (404): Document with identifier 88 does not exist",
                ),
            ),
            startedAtMs = NOW - 4 * 60_000,
            finishedAtMs = NOW - 60_000,
        ),
    ),
)

@Preview @Composable
internal fun OfflineSyncEmptyPreview() = Render(OfflineSyncState(records = emptyList(), nowMs = NOW))

@Preview @Composable
internal fun OfflineSyncOfflinePreview() = Render(
    base.copy(online = false, level = RecordKind.CLIENT, expanded = emptySet()),
)

@Preview @Composable
internal fun OfflineSyncPickerPreview() = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.surface) {
        PickerSheetBody(
            picker = PickerState(
                selected = setOf(
                    SyncTarget(RecordKind.CENTRE, 27, "Centro Las Flores"),
                    SyncTarget(RecordKind.CENTRE, 22, "Centro Morelia Norte"),
                ),
            ),
            rows = ScreenState.Content(
                listOf(
                    PickerRow(SyncTarget(RecordKind.CENTRE, 18, "Arin Savings"), "000000018"),
                    PickerRow(SyncTarget(RecordKind.CENTRE, 10, "Centro Arin"), "000000010"),
                    PickerRow(SyncTarget(RecordKind.CENTRE, 27, "Centro Las Flores"), "000000027"),
                    PickerRow(SyncTarget(RecordKind.CENTRE, 22, "Centro Morelia Norte"), "000000022"),
                ),
            ),
            state = base,
            onAction = {},
        )
    }
}
