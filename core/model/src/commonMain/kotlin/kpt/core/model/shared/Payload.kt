/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable
import kpt.core.model.utils.ApiDateFormatter

// TODO Remove calendarId and TransactionDate from this Payload class;
@Serializable
open class Payload {
    var dateFormat = ApiDateFormatter.DATE_FORMAT
    var locale = ApiDateFormatter.LOCALE
    var calendarId: Long = 0
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
