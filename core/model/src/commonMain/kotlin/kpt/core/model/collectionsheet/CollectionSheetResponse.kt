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
import kpt.core.model.objects.collectionsheets.AttendanceTypeOption
import kpt.core.model.objects.collectionsheets.SavingsProduct
import kpt.core.model.payment.PaymentTypeOption

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `CollectionSheetResponse`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class CollectionSheetResponse(
    /** The attendance choices. */
    var attendanceTypeOptions: List<AttendanceTypeOption> = ArrayList(),
    /** The date the amount is due. */
    var dueDate: IntArray? = null,
    /** The groups. */
    var groups: List<GroupCollectionSheet> = ArrayList(),
    /** The loan products the sheet covers. */
    var loanProducts: List<kpt.core.model.objects.organisations.LoanProducts> = ArrayList(),
    /** The payment types the user may choose from. */
    var paymentTypeOptions: List<PaymentTypeOption> = ArrayList(),
    /** The savings products the sheet covers. */
    var savingsProducts: List<SavingsProduct> = ArrayList(),
)
