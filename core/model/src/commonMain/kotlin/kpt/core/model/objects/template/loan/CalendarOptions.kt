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
data class CalendarOptions(
    /** The server id of this record. */
    var id: Int? = null,

    /** The server id of the calendar instance. */
    var calendarInstanceId: Int? = null,

    /** The server id of the entity this record belongs to. */
    var entityId: Int? = null,

    /** The kind of entity this record belongs to (client, group, loan, …). */
    var entityType: EntityType? = null,

    /** The title. */
    var title: String? = null,

    /** The date this starts. */
    var startDate: List<Int>? = null,

    /** The meeting's length, in minutes. */
    var duration: Int? = null,

    /** The record's type. */
    var type: CalendarOptionsType? = null,

    /** True when the meeting repeats. */
    var repeating: Boolean? = null,

    /** The calendar's recurrence rule (RFC 5545 `RRULE`). */
    var recurrence: String? = null,

    /** The recurrence unit of the meeting (daily, weekly, monthly, …). */
    var frequency: Frequency? = null,

    /** How many recurrence units between meetings. */
    var interval: Int? = null,

    /** Which occurrence in the month the meeting falls on. */
    var repeatsOnNthDayOfMonth: RepeatsOnNthDayOfMonth? = null,

    /** Minutes before the meeting the first reminder fires. */
    var firstReminder: Int? = null,

    /** Minutes before the meeting the second reminder fires. */
    var secondReminder: Int? = null,

    /** The meeting dates the calendar produces. */
    var recurringDates: List<List<Int>>? = null,

    /** The next ten meeting dates. */
    var nextTenRecurringDates: List<List<Int>>? = null,

    /** The recurrence in words (e.g. "Weekly on Tuesday"). */
    var humanReadable: String? = null,

    /** The date the record was created. */
    var createdDate: List<Int>? = null,

    /** The date the calendar was last updated. */
    var lastUpdatedDate: List<Int>? = null,

    /** The server id of the user who created the record. */
    var createdByUserId: Int? = null,

    /** The username of whoever created the record. */
    var createdByUsername: String? = null,

    /** The server id of the user who last updated the calendar. */
    var lastUpdatedByUserId: Int? = null,

    /** The username of whoever last updated the calendar. */
    var lastUpdatedByUsername: String? = null,
)
