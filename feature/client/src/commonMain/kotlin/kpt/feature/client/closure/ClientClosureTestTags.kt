/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.closure

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientClosureTestTags {
    const val SCREEN = "clientClosure:screen"
    const val DATE = "clientClosure:date"
    const val REASON = "clientClosure:reason"
    const val BACK = "clientClosure:back"
    const val SUBMIT = "clientClosure:submit"
    const val NO_REASONS = "clientClosure:noReasons"
    const val STATUS_DIALOG = "clientClosure:statusDialog"
}
