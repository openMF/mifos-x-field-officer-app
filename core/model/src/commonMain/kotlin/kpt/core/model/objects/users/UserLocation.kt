/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.users

import kotlinx.serialization.Serializable
import kpt.core.model.utils.DateConstants

/**
 * Created by Rajan Maurya on 24/01/17.
 */
@Serializable
data class UserLocation(
    var staffId: Int? = null,

    var latLng: String? = null,

    var startTime: String? = null,

    var stopTime: String? = null,

    var date: String? = null,

    var startAddress: String? = null,

    var endAddress: String? = null,

    var dateFormat: String? = DateConstants.DATE_FORMAT_WITH_TIME,

    var locale: String? = DateConstants.LOCALE,
)
