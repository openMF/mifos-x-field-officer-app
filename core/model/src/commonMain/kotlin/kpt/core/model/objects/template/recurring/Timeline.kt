/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
/** A timeline. */
@Serializable
class Timeline(
    /** The first name of whoever submitted the record. */
    val submittedByFirstname: String? = null,
    /** The last name of whoever submitted the record. */
    val submittedByLastname: String? = null,
    /** The username of whoever submitted the record. */
    val submittedByUsername: String? = null,
    /** The date the record was submitted. */
    val submittedOnDate: List<Int>? = null,
)
