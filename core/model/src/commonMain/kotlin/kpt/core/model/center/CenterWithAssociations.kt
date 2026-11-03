/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.center

import kotlinx.serialization.Serializable
import kpt.core.model.client.ClientStatus
import kpt.core.model.group.Group
import kpt.core.model.objects.collectionsheets.CollectionMeetingCalendar
import kpt.core.model.shared.Timeline

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CenterWithAssociations`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class CenterWithAssociations(
    /** The server id of this record. */
    var id: Int? = null,
    /** The account number Fineract assigned. */
    var accountNo: String? = null,
    /** The display name. */
    var name: String? = null,
    /** The id an external system uses for this record, if one was set. */
    var externalId: String? = null,
    /** The server id of the office. */
    var officeId: Int? = null,
    /** The name of the office. */
    var officeName: String? = null,
    /** The server id of the staff member (loan officer). */
    var staffId: Int? = null,
    /** The staff member's display name. */
    var staffName: String? = null,
    /** The office hierarchy path (e.g. `.1.2.`). */
    var hierarchy: String? = null,
    /** The record's Fineract status. */
    var status: ClientStatus? = null,
    /** True when the record is active. */
    var active: Boolean? = null,
    /** The date the record was activated. */
    var activationDate: List<Int> = ArrayList(),
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    var timeline: Timeline? = null,
    /** The groups in the centre. */
    var groupMembers: List<Group> = ArrayList(),
    /** The calendar of the collection meetings. */
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
