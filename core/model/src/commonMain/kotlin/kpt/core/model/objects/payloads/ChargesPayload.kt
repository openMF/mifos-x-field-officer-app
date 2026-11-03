/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.payloads

import kotlinx.serialization.Serializable

/**
 * Created by nellyk on 2/15/2016.
 */
/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */

@Serializable
data class ChargesPayload(
    /** The server id of the charge. */
    var chargeId: Int? = null,
    /** The server id of the client. */
    var clientId: Int? = null,
    /** The server id of the loan. */
    var loanId: Int? = null,
    /** The amount, in the record's currency. */
    var amount: String? = null,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = null,
    /** The date the amount is due. */
    var dueDate: String? = null,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = null,
)
