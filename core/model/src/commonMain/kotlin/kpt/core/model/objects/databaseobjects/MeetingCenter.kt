/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

/** A meeting center. */
data class MeetingCenter(
    /** The display name. */
    var name: String? = null,

    /** The id an external system uses for this record, if one was set. */
    var externalId: String? = null,

    /** The server id of the office. */
    var officeId: Int = 0,

    /** The server id of the staff member (loan officer). */
    var staffId: Int = 0,

    /** The staff member's display name. */
    var staffName: String? = null,

    /** The record's Fineract status. */
    var status: Status? = null,

    /** True when the record is active. */
    var isActive: Boolean = false,

    /** The date of the meeting. */
    var meetingDate: MeetingDate? = null,

    /** The calendar of the collection meetings. */
    var collectionMeetingCalendar: CollectionMeetingCalendar? = null,

    /** Whether the centre has been synced (1) or not (0). */
    var isSynced: Int = 0,

    /** The server id of the centre. */
    var centerId: Long = 0,

    /** The date the record was activated. */
    var activationDate: List<Int>? = null,
)
