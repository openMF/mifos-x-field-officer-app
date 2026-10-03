/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable
import kpt.core.model.objects.Changes
@Serializable
data class LoanRepaymentResponse(
    var officeId: Int? = null,

    var clientId: Int? = null,

    var loanId: Int? = null,

    var resourceId: Int? = null,

    var changes: Changes? = null,
)
