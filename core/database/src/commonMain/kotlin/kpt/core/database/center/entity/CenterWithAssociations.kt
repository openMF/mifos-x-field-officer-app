/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center.entity

import kotlinx.serialization.Serializable
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.database.group.entity.GroupEntity
import kpt.core.model.objects.collectionsheets.CollectionMeetingCalendar
import kpt.core.model.shared.Timeline

/**
 * Created by ishankhanna on 28/06/14.
 */
@Serializable
data class CenterWithAssociations(
    var id: Int? = null,

    var accountNo: String? = null,

    var name: String? = null,

    var externalId: String? = null,

    var officeId: Int? = null,

    var officeName: String? = null,

    var staffId: Int? = null,

    var staffName: String? = null,

    var hierarchy: String? = null,

    var status: ClientStatusEntity? = null,

    var active: Boolean? = null,

    var activationDate: List<Int> = ArrayList(),

    var timeline: Timeline? = null,

    var groupMembers: List<GroupEntity> = ArrayList(),
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
