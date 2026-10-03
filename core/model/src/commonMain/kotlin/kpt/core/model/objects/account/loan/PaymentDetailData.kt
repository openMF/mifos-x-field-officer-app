/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable
@Serializable
data class PaymentDetailData(
    var id: Int? = null,

    var paymentType: PaymentType? = null,

    var accountNumber: String? = null,

    var checkNumber: String? = null,

    var routingCode: String? = null,

    var receiptNumber: String? = null,

    var bankNumber: String? = null,
)
