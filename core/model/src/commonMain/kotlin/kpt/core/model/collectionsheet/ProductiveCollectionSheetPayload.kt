/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    var bulkRepaymentTransactions: MutableList<BulkRepaymentTransactions> = ArrayList(),

    var calendarId: Int? = 0,

    var dateFormat: String? = ApiDateFormatter.DATE_FORMAT,

    var locale: String? = ApiDateFormatter.LOCALE,

    var transactionDate: String? = null,
)
