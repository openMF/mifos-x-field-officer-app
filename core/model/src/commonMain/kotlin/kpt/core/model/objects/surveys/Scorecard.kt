/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.surveys

import kotlinx.serialization.Serializable

/**
 * Created by Nasim Banu on 28,January,2016.
 */
// TODO migrate to KMP date
@Serializable
data class Scorecard(
    var userId: Int = 0,

    var clientId: Int = 0,

//    var createdOn: Date? = null,
    var createdOn: List<Int> = emptyList(),
    var scorecardValues: List<ScorecardValues>? = null,
)
