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
/** A timeline. */
@Serializable
data class Timeline(
    /** The date the record was submitted. */
    var submittedOnDate: List<Int?> = ArrayList(),

    /** The username of whoever submitted the record. */
    var submittedByUsername: String? = null,

    /** The first name of whoever submitted the record. */
    var submittedByFirstname: String? = null,

    /** The last name of whoever submitted the record. */
    var submittedByLastname: String? = null,

    /** The date the record was approved. */
    var approvedOnDate: List<Int?> = ArrayList(),

    /** The username of whoever approved the record. */
    var approvedByUsername: String? = null,

    /** The first name of whoever approved the record. */
    var approvedByFirstname: String? = null,

    /** The last name of whoever approved the record. */
    var approvedByLastname: String? = null,

    /** The date the record was activated. */
    var activatedOnDate: List<Int?> = ArrayList(),
)
