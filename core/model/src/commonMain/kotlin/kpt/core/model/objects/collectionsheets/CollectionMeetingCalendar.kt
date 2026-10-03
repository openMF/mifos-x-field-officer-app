/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 16/07/14.
 */
@Serializable
data class CollectionMeetingCalendar(
    var id: Int = 0,

    var calendarInstanceId: Int? = null,

    var entityId: Int? = null,

    var entityType: EntityType? = null,

    var title: String? = null,

    var startDate: List<Int> = ArrayList(),

    var duration: Int? = null,

    var type: EntityType? = null,

    var repeating: Boolean? = null,

    var recurrence: String? = null,

    var frequency: EntityType? = null,

    var interval: Int? = null,

    var repeatsOnDay: EntityType? = null,

    var firstReminder: Int? = null,

    var secondReminder: Int? = null,

    var recurringDates: List<List<Int>> = ArrayList(),

    var nextTenRecurringDates: List<List<Int>> = ArrayList(),

    var humanReadable: String? = null,

    var recentEligibleMeetingDate: List<Int> = ArrayList(),

    var createdDate: List<Int> = ArrayList(),

    var lastUpdatedDate: List<Int> = ArrayList(),

    var createdByUserId: Int? = null,

    var createdByUsername: String? = null,

    var lastUpdatedByUserId: Int? = null,

    var lastUpdatedByUsername: String? = null,
)
