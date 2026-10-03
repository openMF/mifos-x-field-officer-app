/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.survey.dto

import kpt.core.model.objects.surveys.ScorecardValues
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Created by Nasim Banu on 28,January,2016.
 */
data class ScorecardPayload
@OptIn(ExperimentalTime::class)
constructor(
    var userId: Int = 0,
    var clientId: Int = 0,
    var createdOn: Instant? = null,
    var scorecardValues: List<ScorecardValues>? = null,
)
