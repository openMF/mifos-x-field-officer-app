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

/**
 * Append-only test-tag registry for the charge feature — Compose UI tests and Maestro flows
 * (`assertVisible: { id: "..." }`) resolve these.
 *
 * **APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).** Add new tags at the bottom; retire
 * one only with a `// uitest-tag-retire:` note in the same commit as its callers.
 */
object TestTags {

    object ChargeList {
        const val SCREEN: String = "charge_list_screen"
        const val CONTENT: String = "charge_list_content"
        const val LIST: String = "charge_list"
        const val EMPTY: String = "charge_list_empty"
        const val ADD: String = "charge_list_add"
        const val DELETE_DIALOG: String = "charge_list_delete_dialog"

        /** A failed write renders ABOVE the list, never instead of it. */
        const val WRITE_ERROR: String = "charge_list_write_error"

        fun row(id: Int): String = "charge_list_row_$id"

        /** Progress overlay while a delete is in flight. */
        const val DELETING: String = "charge_list_deleting"
    }

    object ChargeDetail {
        const val SCREEN: String = "charge_detail_screen"
        const val CONTENT: String = "charge_detail_content"
        const val EDIT: String = "charge_detail_edit"
        const val NOT_FOUND: String = "charge_detail_not_found"
    }

    object ChargeForm {
        const val SCREEN: String = "charge_form_screen"
        const val LOADING: String = "charge_form_loading"
        const val TYPES_ERROR: String = "charge_form_types_error"
        const val NO_TYPES: String = "charge_form_no_types"
        const val CHARGE_TYPE: String = "charge_form_charge_type"
        const val CHARGE_TIME: String = "charge_form_charge_time"
        const val CHARGE_CALCULATION: String = "charge_form_charge_calculation"
        const val AMOUNT: String = "charge_form_amount"
        const val DUE_DATE: String = "charge_form_due_date"
        const val SUBMIT: String = "charge_form_submit"
        const val STATUS_DIALOG: String = "charge_form_status_dialog"
        const val SUBMITTING: String = "charge_form_submitting"
    }
}
