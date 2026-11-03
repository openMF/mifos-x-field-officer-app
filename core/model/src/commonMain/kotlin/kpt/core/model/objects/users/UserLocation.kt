/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.users

import kotlinx.serialization.Serializable
import kpt.core.model.utils.DateConstants

/**
 * Created by Rajan Maurya on 24/01/17.
 */
@Serializable
data class UserLocation(
    /** The server id of the staff member (loan officer). */
    var staffId: Int? = null,

    /** The tracked points, serialised. */
    var latLng: String? = null,

    /** When tracking started. */
    var startTime: String? = null,

    /** When tracking stopped. */
    var stopTime: String? = null,

    /** The date the path was tracked. */
    var date: String? = null,

    /** The address where the tracked path started. */
    var startAddress: String? = null,

    /** The address where the tracked path ended. */
    var endAddress: String? = null,

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = DateConstants.DATE_FORMAT_WITH_TIME,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = DateConstants.LOCALE,
)
