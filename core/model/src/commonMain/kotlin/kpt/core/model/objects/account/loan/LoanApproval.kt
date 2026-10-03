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

import kpt.core.model.utils.DateConstants
class LoanApproval(
    var approvedOnDate: String? = null,

    var approvedLoanAmount: String? = null,

    var expectedDisbursementDate: String? = null,

    var note: String? = null,

    var locale: String = DateConstants.LOCALE,

    var dateFormat: String = DateConstants.DATE_FORMAT,
)
