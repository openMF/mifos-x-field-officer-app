/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    /** The server id of the user. */
    var userId: Int = 0,
    /** The server id of the client. */
    var clientId: Int = 0,
    /** When the record was created. */
    var createdOn: Instant? = null,
    /** The answers in the scorecard. */
    var scorecardValues: List<ScorecardValues>? = null,
)
