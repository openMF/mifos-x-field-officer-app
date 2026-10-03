/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import kpt.core.base.store.infra.DraftRecord
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see SettingsScreenPreview.kt for the
 * full rationale.
 *
 * This is the offline-write surface: it is where a user goes to find out what has NOT synced yet.
 * The status split (pending / syncing / failed) is the load-bearing visual — a failed draft that
 * renders like a syncing one tells the user their write is still in flight when it has stopped —
 * so every status gets its own render rather than one representative row.
 *
 * Literals are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

private fun draft(id: Long, status: SubmitOutboxStatus) = DraftRecord(
    id = id,
    formKey = "bill-reminder-create",
    uniqueKey = "bill-$id",
    status = status,
    createdAtMs = 1_700_000_000_000L,
    updatedAtMs = 1_700_000_000_000L,
    errorMessage = if (status == SubmitOutboxStatus.FAILED) "Server rejected the write" else null,
)

@Preview
@Composable
internal fun SyncAndDraftsScreenContentPreview() {
    KptTheme {
        SyncAndDraftsScreenContent(
            uiState = SyncAndDraftsUiState.Success(
                drafts = listOf(draft(1, SubmitOutboxStatus.PENDING)),
                syncing = listOf(draft(2, SubmitOutboxStatus.RETRYING)),
                failed = listOf(draft(3, SubmitOutboxStatus.FAILED)),
            ),
            onBackClick = {},
            onRetry = {},
            onDiscard = {},
            onPrune = {},
        )
    }
}

@Preview
@Composable
internal fun SyncAndDraftsScreenContentEverythingSyncedPreview() {
    // `isEmpty` is the reassuring state — nothing held, nothing failed. It must read as "you're up
    // to date" rather than as a failure to load.
    KptTheme {
        SyncAndDraftsScreenContent(
            uiState = SyncAndDraftsUiState.Success(
                drafts = emptyList(),
                syncing = emptyList(),
                failed = emptyList(),
            ),
            onBackClick = {},
            onRetry = {},
            onDiscard = {},
            onPrune = {},
        )
    }
}

@Preview
@Composable
internal fun SyncAndDraftsScreenContentLoadingPreview() {
    KptTheme {
        SyncAndDraftsScreenContent(
            uiState = SyncAndDraftsUiState.Loading,
            onBackClick = {},
            onRetry = {},
            onDiscard = {},
            onPrune = {},
        )
    }
}

@Preview
@Composable
internal fun DraftRowEveryStatusPreview() {
    // Only the FAILED row gets a retry affordance (`onRetry` is nullable) and an error message.
    // Rendering all three adjacently is what shows those differences actually land.
    KptTheme {
        Column {
            DraftRow(record = draft(1, SubmitOutboxStatus.PENDING), onRetry = null, onDiscard = {})
            DraftRow(record = draft(2, SubmitOutboxStatus.RETRYING), onRetry = null, onDiscard = {})
            DraftRow(record = draft(3, SubmitOutboxStatus.FAILED), onRetry = {}, onDiscard = {})
        }
    }
}

@Preview
@Composable
internal fun StatusChipEveryStatusPreview() {
    KptTheme {
        Column {
            StatusChip(status = SubmitOutboxStatus.PENDING)
            StatusChip(status = SubmitOutboxStatus.RETRYING)
            StatusChip(status = SubmitOutboxStatus.FAILED)
            StatusChip(status = SubmitOutboxStatus.SUBMITTED)
        }
    }
}

@Preview
@Composable
internal fun SyncAndDraftsLoadingStatePreview() {
    KptTheme {
        LoadingState()
    }
}

@Preview
@Composable
internal fun SyncAndDraftsEmptyStatePreview() {
    KptTheme {
        EmptyState()
    }
}

@Preview
@Composable
internal fun ConflictRowCardPreview() {
    // A conflicted write reaches the user as a CHOICE — accept the server's value or retry theirs.
    // Both actions must be visibly distinct, or one side of the write is discarded by accident.
    KptTheme {
        ConflictRowCard(
            conflict = ConflictEntry(
                id = "c-1",
                entity = "CloudTodo",
                key = "1",
                localPayloadJson = """{"completed":true}""",
                serverPayloadJson = """{"completed":false}""",
                formRoute = null,
                recordedAtMs = 1_700_000_000_000L,
            ),
            onAcceptServer = {},
            onRetryLocal = {},
        )
    }
}
