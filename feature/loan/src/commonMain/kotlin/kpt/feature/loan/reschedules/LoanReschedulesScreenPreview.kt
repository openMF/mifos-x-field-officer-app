/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.reschedules

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`.
 */

// i18n:skip — preview fixtures below
private val previewRows = listOf(
    RescheduleRow(
        id = 3,
        status = RescheduleStatusCode.PENDING,
        rescheduleFrom = "15 Oct 2025",
        reason = "Crop failure",
        submittedOn = "09 Oct 2025",
        submittedBy = "Arif Hossain",
    ),
    RescheduleRow(
        id = 1,
        status = RescheduleStatusCode.APPROVED,
        rescheduleFrom = "01 Mar 2025",
        reason = "Medical emergency",
        submittedOn = "20 Feb 2025",
        submittedBy = "Arif Hossain",
    ),
)

@Preview
@Composable
internal fun LoanReschedulesContentPreview() {
    KptTheme {
        LoanReschedulesContent(
            state = LoanReschedulesState(loanId = 7),
            history = ScreenState.Content(previewRows),
            onAction = {},
            canPropose = true,
        )
    }
}

@Preview
@Composable
internal fun LoanReschedulesContentEmptyPreview() {
    KptTheme {
        LoanReschedulesContent(
            state = LoanReschedulesState(loanId = 7),
            history = ScreenState.Empty,
            onAction = {},
            canPropose = true,
        )
    }
}
