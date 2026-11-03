/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.entity

import kotlinx.serialization.Serializable
import kpt.core.database.loan.entity.LoanStatusEntity
import kpt.core.model.objects.collectionsheets.CollectionMeetingCalendar

/**
 * Created by Tarun on 25-07-2017.
 *
 * `@Serializable` because it is both the wire shape of `GET centers?meetingDate=` and the JSON the
 * `meetingFallCenters` column holds. Without it the roster could neither be decoded nor written:
 * `CollectionSheetTypeConverters` threw on every insert, even of an empty list.
 */
@Serializable
data class MeetingFallCalendar(
    // It's not a mistake. This AccountNo field DOES expect a String.
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
    val status: LoanStatusEntity? = null,

    /** The amount collected at the meeting. */
    val totalCollected: Int = 0,

    /** The total amount overdue. */
    val totalOverdue: Int = 0,

    /** The total due at the meeting. */
    val totaldue: Int = 0,
)
