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

import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.model.objects.collectionsheets.AttendanceTypeOption
import kpt.core.model.objects.collectionsheets.SavingsProduct

/**
 * Created by Tarun on 25-07-2017.
 */
data class CollectionSheetResponse(
    var attendanceTypeOptions: List<AttendanceTypeOption> = ArrayList(),

    var dueDate: IntArray? = null,

    var groups: List<GroupCollectionSheet> = ArrayList(),

    var loanProducts: List<kpt.core.model.objects.organisations.LoanProducts> = ArrayList(),

    var paymentTypeOptions: List<PaymentTypeOptionEntity> = ArrayList(),

    var savingsProducts: List<SavingsProduct> = ArrayList(),
)
