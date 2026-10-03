/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

data class MeetingCenter(
    var name: String? = null,

    var externalId: String? = null,

    var officeId: Int = 0,

    var staffId: Int = 0,

    var staffName: String? = null,

    var status: Status? = null,

    var isActive: Boolean = false,

    var meetingDate: MeetingDate? = null,

    var collectionMeetingCalendar: CollectionMeetingCalendar? = null,

    var isSynced: Int = 0,

    var centerId: Long = 0,

    var activationDate: List<Int>? = null,
)
