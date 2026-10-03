/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kpt.core.model.utils.DateConstants

/**
 * Created by Tarun on 25-07-2017.
 */
data class CollectionSheetRequestPayload(
    var calendarId: Int? = null,

    var dateFormat: String = DateConstants.DATE_FORMAT,

    var locale: String = DateConstants.LOCALE,

    var transactionDate: String? = null,
)
