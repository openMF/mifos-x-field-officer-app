/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet.payload

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.model.shared.BulkRepaymentTransactions
import kpt.core.model.shared.Payload

/** Request body for a collection sheet. */
@Serializable
class CollectionSheetPayload : Payload() {
    /** The date the loan was actually disbursed. */
    var actualDisbursementDate: String? = null

    /** The loan disbursements made at the meeting. */
    var bulkDisbursementTransactions: List<Int> = emptyList()

    /** The loan repayments collected at the meeting. */
    var bulkRepaymentTransactions: List<BulkRepaymentTransactions> = emptyList()

    /** The attendance recorded for each client. */
    var clientsAttendance: List<String> = emptyList()

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}
