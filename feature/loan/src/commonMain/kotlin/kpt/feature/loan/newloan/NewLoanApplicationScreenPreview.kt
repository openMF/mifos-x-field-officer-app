/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.newloan

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
private val previewProducts = listOf(
    LoanProductOption(id = 1, name = "Business Loan"),
    LoanProductOption(id = 2, name = "Housing Loan"),
)

@Preview
@Composable
internal fun NewLoanApplicationContentPreview() {
    KptTheme {
        NewLoanApplicationContent(
            state = NewLoanApplicationState(clientId = 42, step = LoanStep.DETAILS),
            products = ScreenState.Content(previewProducts),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun NewLoanApplicationContentProductsLoadingPreview() {
    KptTheme {
        NewLoanApplicationContent(
            state = NewLoanApplicationState(clientId = 42),
            products = ScreenState.Loading,
            onAction = {},
        )
    }
}
