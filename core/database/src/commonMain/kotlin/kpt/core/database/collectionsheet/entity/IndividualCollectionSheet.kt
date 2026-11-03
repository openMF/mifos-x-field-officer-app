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
import kpt.core.model.objects.account.loan.PaymentTypeOptions

/**
 * Created by Tarun on 06-07-2017.
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
