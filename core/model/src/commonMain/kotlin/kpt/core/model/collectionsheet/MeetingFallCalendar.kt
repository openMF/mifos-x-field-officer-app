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
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The date the record was activated. */
    val activationDate: List<Int> = emptyList(),
    /** True when the record is active. */
    val isActive: Boolean = false,
    /** The calendar of the collection meetings. */
    val collectionMeetingCalendar: CollectionMeetingCalendar? = null,
    /** The office hierarchy path (e.g. `.1.2.`). */
    val hierarchy: String? = null,
    /** The server id of this record. */
    val id: Int = 0,
    /** The installment amount due at the meeting. */
    val installmentDue: Int = 0,
    /** The display name. */
    val name: String? = null,
    /** The server id of the office. */
    val officeId: Int = 0,
    /** The server id of the staff member (loan officer). */
    val staffId: Int = 0,
    /** The staff member's display name. */
    val staffName: String? = null,
    /** The record's Fineract status. */
    val status: LoanStatus? = null,
    /** The amount collected at the meeting. */
    val totalCollected: Int = 0,
    /** The total amount overdue. */
    val totalOverdue: Int = 0,
    /** The total due at the meeting. */
    val totaldue: Int = 0,
)
