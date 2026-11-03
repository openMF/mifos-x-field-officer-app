/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.statuschange

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screen is not
 * previewed; its stateless `*Content` half is. Values are server content, not app copy —
 * `// i18n:skip`. Every date is the fixed [PREVIEW_DATE_MILLIS]: several states default theirs to
 * `Clock.System.now()`, which would make the golden change on every run.
 */

/** 2025-10-09 — fixed so a golden never changes because the clock moved. */
private const val PREVIEW_DATE_MILLIS = 1_760_000_000_000L

// i18n:skip — preview fixtures below
@Preview
@Composable
internal fun SavingsStatusChangeContentApprovePreview() {
    // APPROVE carries a note; ACTIVATE does not (`hasNote`) — both are previewed for that reason.
    KptTheme {
        SavingsStatusChangeContent(
            state = SavingsStatusChangeState(
                accountId = 9,
                change = SavingsStatusChange.APPROVE,
                date = PREVIEW_DATE_MILLIS,
                note = "Documents verified",
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun SavingsStatusChangeContentActivatePreview() {
    KptTheme {
        SavingsStatusChangeContent(
            state = SavingsStatusChangeState(
                accountId = 9,
                change = SavingsStatusChange.ACTIVATE,
                date = PREVIEW_DATE_MILLIS,
            ),
            onAction = {},
        )
    }
}
