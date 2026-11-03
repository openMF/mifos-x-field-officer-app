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

/** A collection meeting calendar. */
data class CollectionMeetingCalendar(
    /** The server id of the calendar instance. */
    var calendarInstanceId: Int = 0,

    /** The server id of the meeting calendar. */
    var calendarId: Long = 0,

    /** The server id of the entity this record belongs to. */
    var entityId: Int = 0,

    /** The kind of entity this record belongs to (client, group, loan, …). */
    var entityType: EntityType? = null,

    /** The title. */
    var title: String? = null,

    /** A free-text description. */
    var description: String? = null,

    /** Where the meeting takes place. */
    var location: String? = null,

    /** The date of the meeting. */
    var meetingCalendarDate: MeetingDate? = null,

    /** True when the meeting repeats. */
    var isRepeating: Boolean = false,

    /** The calendar's recurrence rule (RFC 5545 `RRULE`). */
    var recurrence: String? = null,

    /** The date this starts. */
    var startDate: List<Int> = ArrayList(),
)
