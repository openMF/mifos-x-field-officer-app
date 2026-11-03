/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.surveys

import kotlinx.serialization.Serializable

/**
 * Created by Nasim Banu on 28,January,2016.
 */
// TODO migrate to KMP date
@Serializable
data class Scorecard(
    /** The server id of the user. */
    var userId: Int = 0,

    /** The server id of the client. */
    var clientId: Int = 0,

//    var createdOn: Date? = null,
    /** When the record was created. */
    var createdOn: List<Int> = emptyList(),
    /** The answers in the scorecard. */
    var scorecardValues: List<ScorecardValues>? = null,
)
