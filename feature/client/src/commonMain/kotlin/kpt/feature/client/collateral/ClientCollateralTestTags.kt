/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.collateral

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientCollateralTestTags {
    const val SCREEN = "clientCollateral:screen"
    const val TYPE = "clientCollateral:type"
    const val QUANTITY = "clientCollateral:quantity"
    const val TOTAL = "clientCollateral:total"
    const val TOTAL_COLLATERAL = "clientCollateral:totalCollateral"
    const val BUTTONS = "clientCollateral:buttons"
    const val NO_OPTIONS = "clientCollateral:noOptions"
    const val STATUS_DIALOG = "clientCollateral:statusDialog"
}
