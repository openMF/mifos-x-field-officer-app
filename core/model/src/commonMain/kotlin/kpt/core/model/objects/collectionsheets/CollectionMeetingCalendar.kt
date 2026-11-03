/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 16/07/14.
 */
@Serializable
data class CollectionMeetingCalendar(
    /** The server id of this record. */
    var id: Int = 0,

    /** The server id of the calendar instance. */
    var calendarInstanceId: Int? = null,

    /** The server id of the entity this record belongs to. */
    var entityId: Int? = null,

    /** The kind of entity this record belongs to (client, group, loan, …). */
    var entityType: EntityType? = null,

    /** The title. */
    var title: String? = null,

    /** The date this starts. */
    var startDate: List<Int> = ArrayList(),

    /** The meeting's length, in minutes. */
    var duration: Int? = null,

    /** The record's type. */
    var type: EntityType? = null,

    /** True when the meeting repeats. */
    var repeating: Boolean? = null,

    /** The calendar's recurrence rule (RFC 5545 `RRULE`). */
    var recurrence: String? = null,

    /** The recurrence unit of the meeting (daily, weekly, monthly, …). */
    var frequency: EntityType? = null,

    /** How many recurrence units between meetings. */
    var interval: Int? = null,

    /** The weekday the meeting repeats on. */
    var repeatsOnDay: EntityType? = null,

    /** Minutes before the meeting the first reminder fires. */
    var firstReminder: Int? = null,

    /** Minutes before the meeting the second reminder fires. */
    var secondReminder: Int? = null,

    /** The meeting dates the calendar produces. */
    var recurringDates: List<List<Int>> = ArrayList(),

    /** The next ten meeting dates. */
    var nextTenRecurringDates: List<List<Int>> = ArrayList(),

    /** The recurrence in words (e.g. "Weekly on Tuesday"). */
    var humanReadable: String? = null,
    /** Fineract `meetingTime` — an `[h, m]` array or an `"HH:mm[:ss]"` string; read through a tolerant parser. */
    var meetingTime: kotlinx.serialization.json.JsonElement? = null,

    /** The most recent meeting date a collection can be recorded for. */
    var recentEligibleMeetingDate: List<Int> = ArrayList(),

    /** The date the record was created. */
    var createdDate: List<Int> = ArrayList(),

    /** The date the calendar was last updated. */
    var lastUpdatedDate: List<Int> = ArrayList(),

    /** The server id of the user who created the record. */
    var createdByUserId: Int? = null,

    /** The username of whoever created the record. */
    var createdByUsername: String? = null,

    /** The server id of the user who last updated the calendar. */
    var lastUpdatedByUserId: Int? = null,

    /** The username of whoever last updated the calendar. */
    var lastUpdatedByUsername: String? = null,
)
