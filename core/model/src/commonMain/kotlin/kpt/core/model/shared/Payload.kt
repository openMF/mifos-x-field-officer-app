/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable
import kpt.core.model.utils.ApiDateFormatter

// TODO Remove calendarId and TransactionDate from this Payload class;
/** Request body for creating an entity. */
@Serializable
open class Payload {
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat = ApiDateFormatter.DATE_FORMAT

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale = ApiDateFormatter.LOCALE

    /** The server id of the meeting calendar. */
    var calendarId: Long = 0

    /** The date of the transaction. */
    var transactionDate: String? = null
    override fun toString(): String {
        return "{" +
            "dateFormat='" + dateFormat + '\'' +
            ", locale='" + locale + '\'' +
            ", calendarId=" + calendarId +
            ", transactionDate='" + transactionDate + '\'' +
            '}'
    }
}
