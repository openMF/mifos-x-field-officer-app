/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.ui

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.charge.ChargeResource
import kpt.feature.charge.detail.ChargeDetailContent
import kpt.feature.charge.form.ChargeFormContent
import kpt.feature.charge.form.ChargeFormState
import kpt.feature.charge.form.ChargeType
import kpt.feature.charge.list.ChargeListContent
import kpt.feature.charge.list.ChargeListState
import kpt.feature.charge.list.ChargeWriteError
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` discovers these from commonMain and `ChargePreviewScreenshotTest`
 * renders them off `desktopTest`. Only the stateless `*Content` halves are previewed — the `*Screen`
 * entries resolve a ViewModel through Koin.
 *
 * Literals are PREVIEW FIXTURE DATA (charge names a tenant configures), never app copy — hence
 * `// i18n:skip` rather than string resources.
 */

internal fun previewCharge(
    id: Int = 1,
    // i18n:skip — preview fixture
    name: String = "Processing Fee",
    outstanding: String = "BDT 150.00",
) = ChargeUi(
    id = id,
    definitionId = "11",
    name = name,
    // i18n:skip — server-supplied values in production
    time = "Specified due date",
    calculation = "Flat",
    dueDate = "14 March 2026",
    amount = "BDT 150.00",
    amountPaid = "BDT 0.00",
    amountWaived = "BDT 0.00",
    outstanding = outstanding,
    penalty = false,
)

private val previewRows = listOf(
    previewCharge(),
    // i18n:skip — preview fixture
    previewCharge(id = 2, name = "Late Payment Penalty", outstanding = "BDT 0.00").copy(penalty = true, dueDate = null),
)

@Preview
@Composable
internal fun ChargeListContentPreview() {
    KptTheme {
        ChargeListContent(
            charges = ScreenState.Content(previewRows),
            ui = ChargeListState(),
            canEdit = true,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ChargeListRowExpandedClientPreview() {
    // A CLIENT row: View Details + Delete only — Fineract has no update for a client charge, so
    // Edit must be absent rather than offered and then refused.
    KptTheme {
        ChargeListContent(
            charges = ScreenState.Content(previewRows),
            ui = ChargeListState(expandedId = 1),
            canEdit = false,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ChargeListContentEmptyPreview() {
    KptTheme {
        ChargeListContent(charges = ScreenState.Empty, ui = ChargeListState(), canEdit = true, onAction = {})
    }
}

@Preview
@Composable
internal fun ChargeListWriteErrorKeepsTheChargesPreview() {
    // A failed delete renders ABOVE the list, not instead of it — the charges are still correct.
    KptTheme {
        ChargeListContent(
            charges = ScreenState.Content(previewRows),
            ui = ChargeListState(writeError = ChargeWriteError.Offline),
            canEdit = true,
            onAction = {},
        )
    }
}

// No preview of the delete confirmation: an AlertDialog is a second compose ROOT, which the
// single-root `captureRoboImage(onRoot())` harness cannot capture.

@Preview
@Composable
internal fun ChargeDetailContentPreview() {
    KptTheme {
        ChargeDetailContent(charge = ScreenState.Content(previewCharge()), canEdit = true, onAction = {})
    }
}

@Preview
@Composable
internal fun ChargeDetailNotFoundPreview() {
    KptTheme {
        ChargeDetailContent(charge = ScreenState.Empty, canEdit = true, onAction = {})
    }
}

private val previewType = ChargeType(
    id = 11,
    // i18n:skip — preview fixture
    name = "Processing Fee",
    amount = 150.0,
    time = "Specified due date",
    calculation = "Flat",
    needsDueDate = true,
)

@Preview
@Composable
internal fun ChargeFormAddPreview() {
    KptTheme {
        ChargeFormContent(
            state = ChargeFormState(
                resource = ChargeResource.LOAN,
                resourceId = 1,
                chargeTypes = listOf(previewType),
                selectedCharge = previewType,
                amount = "150",
                dueDate = PREVIEW_DATE,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ChargeFormEditPreview() {
    KptTheme {
        ChargeFormContent(
            state = ChargeFormState(
                resource = ChargeResource.SAVINGS,
                resourceId = 1,
                editingId = 5,
                chargeTypes = listOf(previewType),
                selectedCharge = previewType,
                amount = "200",
                dueDate = PREVIEW_DATE,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ChargeFormInvalidAmountPreview() {
    KptTheme {
        ChargeFormContent(
            state = ChargeFormState(
                chargeTypes = listOf(previewType),
                selectedCharge = previewType.copy(needsDueDate = false),
                amount = "0",
                amountError = true,
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun ChargeFormNoTypesPreview() {
    // The sandbox tenant ships with NO charge definitions — the state an officer most often sees.
    KptTheme {
        ChargeFormContent(state = ChargeFormState(), onAction = {})
    }
}

/** 2026-03-14 UTC — fixed, so the rendered due date never drifts between golden runs. */
private const val PREVIEW_DATE = 1_773_446_400_000L
