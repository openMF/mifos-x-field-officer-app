/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.checkerinbox.ui

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask
import kpt.core.model.objects.checkerinboxtask.RescheduleReasonCodeValue
import org.jetbrains.compose.ui.tooling.preview.Preview

// Noon UTC: the rendered date is the same in every time zone a golden is recorded in.
private const val OCT_20_2026 = 1_792_497_600_000L
private const val DAY = 86_400_000L

// i18n:skip — preview fixtures
private val previewTasks = listOf(
    CheckerTask(41, OCT_20_2026, "awaiting.approval", "asha.r", "CREATE", "CLIENT", "1043"),
    CheckerTask(40, OCT_20_2026 - DAY, "awaiting.approval", "bimal.d", "APPROVE", "LOAN", "877"),
    CheckerTask(37, OCT_20_2026 - 3 * DAY, "awaiting.approval", "asha.r", "DISBURSE", "LOAN", "861"),
)

// i18n:skip — preview fixtures
private val previewReschedules = listOf(
    RescheduleLoansTask(
        4,
        77,
        "Asha Rahman",
        "000000077",
        listOf(2026, 11, 5),
        rescheduleReasonCodeValue = RescheduleReasonCodeValue(21, "Crop failure"),
    ),
    RescheduleLoansTask(5, 78, "Bimal Das", "000000078", listOf(2026, 12, 1)),
)

private val previewTemplate = CheckerInboxSearchTemplate(
    actionNames = listOf("CREATE", "APPROVE"),
    entityNames = listOf("CLIENT", "LOAN"),
)

@Composable
private fun Inbox(state: CheckerInboxState, tasks: List<CheckerTask> = previewTasks, total: Int = previewTasks.size) {
    KptTheme {
        CheckerInboxContent(
            state = state,
            inbox = ScreenState.Content(InboxView(tasks, total)),
            reschedules = ScreenState.Content(previewReschedules),
            template = previewTemplate,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CheckerInboxPreview() = Inbox(CheckerInboxState(expanded = 40))

@Preview
@Composable
internal fun CheckerInboxSelectionPreview() = Inbox(CheckerInboxState(selected = setOf(41, 37)))

@Preview
@Composable
internal fun CheckerInboxNoMatchPreview() =
    Inbox(CheckerInboxState(query = "zz", filter = InboxFilter(entity = "SAVINGS")), tasks = emptyList())

@Preview
@Composable
internal fun CheckerInboxEmptyPreview() = Inbox(CheckerInboxState(), tasks = emptyList(), total = 0)

@Preview
@Composable
internal fun CheckerInboxReschedulesPreview() = Inbox(CheckerInboxState(tab = InboxTab.RESCHEDULES))

@Preview
@Composable
internal fun CheckerInboxOutcomePreview() = Inbox(
    CheckerInboxState(
        selected = setOf(40),
        // i18n:skip — preview fixture
        outcome = DecisionOutcome(
            Decision.APPROVE,
            done = 2,
            failures = listOf(DecisionFailure("#40 APPROVE LOAN", "Maker cannot check own task")),
            offline = false,
        ),
    ),
    // The two approved tasks have left the inbox; the failed one stays, selected for a retry.
    tasks = previewTasks.filter { it.id == 40 },
    total = 1,
)
