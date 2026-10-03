/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.entity

import kpt.core.database.loan.entity.LoanStatusEntity
import kpt.core.model.objects.collectionsheets.CollectionMeetingCalendar

/**
 * Created by Tarun on 25-07-2017.
 */
data class MeetingFallCalendar(
    // It's not a mistake. This AccountNo field DOES expect a String.
    val accountNo: String? = null,

    val activationDate: List<Int>,

    val isActive: Boolean = false,
    val collectionMeetingCalendar: CollectionMeetingCalendar? = null,

    val hierarchy: String? = null,

    val id: Int = 0,

    val installmentDue: Int = 0,

    val name: String? = null,

    val officeId: Int = 0,

    val staffId: Int = 0,

    val staffName: String? = null,

    val status: LoanStatusEntity? = null,

    val totalCollected: Int = 0,

    val totalOverdue: Int = 0,

    val totaldue: Int = 0,
)
