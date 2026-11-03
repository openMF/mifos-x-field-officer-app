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

    /** Tags for the charge list screen. */
    object ChargeList {
        /** The charge list screen's root. */
        const val SCREEN: String = "charge_list_screen"

        /** The charge list's loaded content. */
        const val CONTENT: String = "charge_list_content"

        /** The list of charges. */
        const val LIST: String = "charge_list"

        /** The empty state shown when the account has no charges. */
        const val EMPTY: String = "charge_list_empty"

        /** The "add charge" button. */
        const val ADD: String = "charge_list_add"

        /** The confirm dialog for deleting a charge. */
        const val DELETE_DIALOG: String = "charge_list_delete_dialog"

        /** A failed write renders ABOVE the list, never instead of it. */
        const val WRITE_ERROR: String = "charge_list_write_error"

        /** One charge row, keyed by the charge id. */
        fun row(id: Int): String = "charge_list_row_$id"

        /** Progress overlay while a delete is in flight. */
        const val DELETING: String = "charge_list_deleting"
    }

    /** Tags for the charge detail screen. */
    object ChargeDetail {
        /** The charge detail screen's root. */
        const val SCREEN: String = "charge_detail_screen"

        /** The charge detail's loaded content. */
        const val CONTENT: String = "charge_detail_content"

        /** The "edit charge" action. */
        const val EDIT: String = "charge_detail_edit"

        /** The state shown when the charge no longer exists. */
        const val NOT_FOUND: String = "charge_detail_not_found"
    }

    /** Tags for the add/edit charge form. */
    object ChargeForm {
        /** The charge form's root. */
        const val SCREEN: String = "charge_form_screen"

        /** The form's loading state while charge types load. */
        const val LOADING: String = "charge_form_loading"

        /** The error shown when the charge types failed to load. */
        const val TYPES_ERROR: String = "charge_form_types_error"

        /** The state shown when the product offers no charge types. */
        const val NO_TYPES: String = "charge_form_no_types"

        /** The charge-type picker. */
        const val CHARGE_TYPE: String = "charge_form_charge_type"

        /** The charge-time field (when the charge is applied). */
        const val CHARGE_TIME: String = "charge_form_charge_time"

        /** The charge-calculation field (flat, percentage, …). */
        const val CHARGE_CALCULATION: String = "charge_form_charge_calculation"

        /** The amount field. */
        const val AMOUNT: String = "charge_form_amount"

        /** The due-date field. */
        const val DUE_DATE: String = "charge_form_due_date"

        /** The submit button. */
        const val SUBMIT: String = "charge_form_submit"

        /** The dialog reporting the submit's outcome. */
        const val STATUS_DIALOG: String = "charge_form_status_dialog"

        /** The progress shown while the charge is sent. */
        const val SUBMITTING: String = "charge_form_submitting"
    }
}
