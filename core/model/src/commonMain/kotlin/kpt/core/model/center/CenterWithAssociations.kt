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
    var id: Int? = null,
    var accountNo: String? = null,
    var name: String? = null,
    var externalId: String? = null,
    var officeId: Int? = null,
    var officeName: String? = null,
    var staffId: Int? = null,
    var staffName: String? = null,
    var hierarchy: String? = null,
    var status: ClientStatus? = null,
    var active: Boolean? = null,
    var activationDate: List<Int> = ArrayList(),
    var timeline: Timeline? = null,
    var groupMembers: List<Group> = ArrayList(),
    var collectionMeetingCalendar: CollectionMeetingCalendar = CollectionMeetingCalendar(),
)
