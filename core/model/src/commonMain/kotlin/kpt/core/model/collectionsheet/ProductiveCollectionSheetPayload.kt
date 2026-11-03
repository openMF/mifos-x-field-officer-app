/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet

import kotlinx.serialization.Serializable
import kpt.core.model.shared.BulkRepaymentTransactions
import kpt.core.model.utils.ApiDateFormatter

/**
 * Created by Tarun on 25-07-2017.
 */
@Serializable
data class ProductiveCollectionSheetPayload(
    /** The loan repayments collected at the meeting. */
    var bulkRepaymentTransactions: MutableList<BulkRepaymentTransactions> = ArrayList(),

    /** The server id of the meeting calendar. */
    var calendarId: Int? = 0,

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = ApiDateFormatter.DATE_FORMAT,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = ApiDateFormatter.LOCALE,

    /** The date of the transaction. */
    var transactionDate: String? = null,
)
