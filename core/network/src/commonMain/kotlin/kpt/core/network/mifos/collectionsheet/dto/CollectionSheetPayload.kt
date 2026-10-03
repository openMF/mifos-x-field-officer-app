/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.collectionsheet.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.model.shared.BulkRepaymentTransactions
import kpt.core.model.shared.Payload

@Serializable
class CollectionSheetPayload : Payload() {
    var actualDisbursementDate: String? = null
    var bulkDisbursementTransactions: List<Int> = emptyList()
    var bulkRepaymentTransactions: List<BulkRepaymentTransactions> = emptyList()
    var clientsAttendance: List<String> = emptyList()

    override fun toString(): String {
        return Json.encodeToString(this)
    }
}
