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

/**
 * Created by Tarun on 06-07-2017.
 */
@Serializable
data class RequestCollectionSheetPayload(
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = null,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = null,
    /** The server id of the office. */
    var officeId: Int? = null,
    /** The server id of the staff member (loan officer). */
    var staffId: Int? = null,
    /** The date of the transaction. */
    var transactionDate: String = "",
)
