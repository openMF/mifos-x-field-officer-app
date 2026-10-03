/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class ChargeOptions(
    var id: Int? = null,

    var name: String? = null,

    var active: Boolean? = null,

    var penalty: Boolean? = null,

    var currency: Currency? = null,

    var amount: Double? = null,

    var chargeTimeType: ChargeTimeType? = null,

    var chargeAppliesTo: ChargeAppliesTo? = null,

    var chargeCalculationType: ChargeCalculationType? = null,

    var chargePaymentMode: ChargePaymentMode? = null,

    var taxGroup: TaxGroup? = null,
)
