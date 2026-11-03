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
 * Created by mayankjindal on 02/10/16.
 */
@Serializable
data class GroupTimeline(
    /** The date the record was submitted. */
    var submittedOnDate: List<Int>? = null,

    /** The username of whoever submitted the record. */
    var submittedByUsername: String? = null,

    /** The first name of whoever submitted the record. */
    var submittedByFirstname: String? = null,

    /** The last name of whoever submitted the record. */
    var submittedByLastname: String? = null,

    /** The date the record was activated. */
    var activatedOnDate: List<Int>? = null,

    /** The username of whoever activated the record. */
    var activatedByUsername: String? = null,

    /** The first name of whoever activated the record. */
    var activatedByFirstname: String? = null,

    /** The last name of whoever activated the record. */
    var activatedByLastname: String? = null,
)
