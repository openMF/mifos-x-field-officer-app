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
import kpt.core.model.objects.account.loan.PaymentTypeOptions

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `IndividualCollectionSheet`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class IndividualCollectionSheet(
    /** The date the amount is due. */
    var dueDate: String? = null,
    /** The clients. */
    var clients: ArrayList<ClientCollectionSheet>? = null,
    /** The payment types the user may choose from. */
    var paymentTypeOptions: ArrayList<PaymentTypeOptions>? = null,
)
