/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.saving

import kotlinx.serialization.Serializable
/** Kind of an interest calculation. */
@Serializable
data class InterestCalculationType(
    /** The server id of this record. */
    var id: Int? = null,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    var code: String? = null,

    /** The human-readable label Fineract sends for this value. */
    var value: String? = null,
)
