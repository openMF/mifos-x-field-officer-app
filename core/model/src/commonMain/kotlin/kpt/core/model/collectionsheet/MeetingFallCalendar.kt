/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet

import kotlinx.serialization.Serializable
import kpt.core.model.loan.LoanStatus
import kpt.core.model.objects.collectionsheets.CollectionMeetingCalendar

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `MeetingFallCalendar`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class MeetingFallCalendar(
    val accountNo: String? = null,
    val activationDate: List<Int> = emptyList(),
    val isActive: Boolean = false,
    val collectionMeetingCalendar: CollectionMeetingCalendar? = null,
    val hierarchy: String? = null,
    val id: Int = 0,
    val installmentDue: Int = 0,
    val name: String? = null,
    val officeId: Int = 0,
    val staffId: Int = 0,
    val staffName: String? = null,
    val status: LoanStatus? = null,
    val totalCollected: Int = 0,
    val totalOverdue: Int = 0,
    val totaldue: Int = 0,
)
