/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class LoanOfficerOption(
    /** The server id of this record. */
    var id: Int? = null,

    /** The person's first name. */
    var firstname: String? = null,

    /** The person's last name. */
    var lastname: String? = null,

    /** The display name. */
    var displayName: String? = null,

    /** The mobile phone number. */
    var mobileNo: String? = null,

    /** The server id of the office. */
    var officeId: Int? = null,

    /** The name of the office. */
    var officeName: String? = null,

    /** True when the staff member is a loan officer. */
    var isLoanOfficer: Boolean? = null,

    /** True when the record is active. */
    var isActive: Boolean? = null,

    /** The date the staff member joined. */
    var joiningDate: List<Int>? = null,
)
