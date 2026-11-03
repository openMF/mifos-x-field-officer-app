/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import kpt.core.model.shared.Timeline

/**
 * Created by ishankhanna on 09/02/14.
 */
data class PageItem(
    /** The server id of this record. */
    val id: Int = 0,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The record's Fineract status. */
    val status: ClientStatusEntity? = null,

    /** True when the record is active. */
    val isActive: Boolean = false,

    /** The date the record was activated. */
    val activationDate: List<Int> = emptyList(),

    /** The person's first name. */
    val firstname: String? = null,

    /** The person's middle name. */
    val middlename: String? = null,

    /** The person's last name. */
    val lastname: String? = null,

    /** The display name. */
    val displayName: String? = null,

    /** The server id of the office. */
    val officeId: Int = 0,

    /** The name of the office. */
    val officeName: String? = null,

    /** The server id of the staff member (loan officer). */
    val staffId: Int = 0,

    /** The staff member's display name. */
    val staffName: String? = null,

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,

    /** The full name. */
    val fullname: String? = null,

    /** The server id of the client's photo. */
    val imageId: Int = 0,

    /** True when the client has a photo. */
    val isImagePresent: Boolean = false,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
)
