/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Serializable
/** A timeline. */
@Serializable
data class Timeline(
    /** The date the record was submitted. */
    val submittedOnDate: List<Int> = emptyList(),
    /** The username of whoever submitted the record. */
    val submittedByUsername: String? = null,
    /** The first name of whoever submitted the record. */
    val submittedByFirstname: String? = null,
    /** The last name of whoever submitted the record. */
    val submittedByLastname: String? = null,
    /** The date the record was activated. */
    val activatedOnDate: List<Int> = emptyList(),
    /** The username of whoever activated the record. */
    val activatedByUsername: String? = null,
    /** The first name of whoever activated the record. */
    val activatedByFirstname: String? = null,
    /** The last name of whoever activated the record. */
    val activatedByLastname: String? = null,
    /** The date the record was closed. */
    val closedOnDate: List<Int> = emptyList(),
    /** The username of whoever closed the record. */
    val closedByUsername: String? = null,
    /** The first name of whoever closed the record. */
    val closedByFirstname: String? = null,
    /** The last name of whoever closed the record. */
    val closedByLastname: String? = null,
)
