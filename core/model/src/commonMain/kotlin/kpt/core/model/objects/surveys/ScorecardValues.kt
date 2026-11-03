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
@Serializable
data class ScorecardValues(
    /** The server id of the question. */
    var questionId: Int? = null,

    /** The server id of the response. */
    var responseId: Int? = null,

    /** The score given for the answer. */
    var value: Int? = null,
)
