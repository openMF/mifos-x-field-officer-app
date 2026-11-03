/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.client

import kotlinx.serialization.Serializable

/**
 * Created by mayankjindal on 13/12/16.
 */

@Serializable
data class ChargePaymentMode(
    /** The server id of this record. */
    val id: Int?,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String?,
    /** The human-readable label Fineract sends for this value. */
    val value: String?,
) {
    override fun toString(): String {
        return "ChargePaymentMode(id=$id, code='$code', value='$value')"
    }
}
