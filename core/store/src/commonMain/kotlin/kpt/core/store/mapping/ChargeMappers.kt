/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.mapping

import kpt.core.model.charge.ChargeCalculationType
import kpt.core.model.charge.ChargeTimeType
import kpt.core.model.charge.Charges
import kpt.core.model.payment.PaymentTypeOption
import kpt.core.model.savings.Charge
import kpt.core.database.charge.entity.ChargeCalculationTypeEntity as ChargeCalculationTypeRow
import kpt.core.database.charge.entity.ChargeTimeTypeEntity as ChargeTimeTypeRow
import kpt.core.database.charge.entity.ChargesEntity as ChargesRow
import kpt.core.database.payment.entity.PaymentTypeOptionEntity as PaymentTypeOptionRow
import kpt.core.database.savings.entity.Charge as ChargeRow

/** Maps the stored row to its `Charge` model. */
fun ChargeRow.toModel(): Charge = Charge(
    id = id,
    chargeId = chargeId,
    accountId = accountId,
    name = name,
    chargeTimeType = chargeTimeType?.toModel(),
    chargeCalculationType = chargeCalculationType?.toModel(),
    percentage = percentage,
    amountPercentageAppliedTo = amountPercentageAppliedTo,
    currency = currency?.toModel(),
    amount = amount,
    amountPaid = amountPaid,
    amountWaived = amountWaived,
    amountWrittenOff = amountWrittenOff,
    amountOutstanding = amountOutstanding,
    amountOrPercentage = amountOrPercentage,
    penalty = penalty,
    additionalProperties = additionalProperties,
)

/** Maps the model to its stored `ChargeRow`. */
fun Charge.toEntity(): ChargeRow = ChargeRow(
    id = id,
    chargeId = chargeId,
    accountId = accountId,
    name = name,
    chargeTimeType = chargeTimeType?.toEntity(),
    chargeCalculationType = chargeCalculationType?.toEntity(),
    percentage = percentage,
    amountPercentageAppliedTo = amountPercentageAppliedTo,
    currency = currency?.toEntity(),
    amount = amount,
    amountPaid = amountPaid,
    amountWaived = amountWaived,
    amountWrittenOff = amountWrittenOff,
    amountOutstanding = amountOutstanding,
    amountOrPercentage = amountOrPercentage,
    penalty = penalty,
    additionalProperties = additionalProperties,
)

/** Maps the stored row to its `ChargeCalculationType` model. */
fun ChargeCalculationTypeRow.toModel(): ChargeCalculationType = ChargeCalculationType(
    id = id,
    code = code,
    value = value,
)

/** Maps the model to its stored `ChargeCalculationTypeRow`. */
fun ChargeCalculationType.toEntity(): ChargeCalculationTypeRow = ChargeCalculationTypeRow(
    id = id,
    code = code,
    value = value,
)

/** Maps the stored row to its `ChargeTimeType` model. */
fun ChargeTimeTypeRow.toModel(): ChargeTimeType = ChargeTimeType(
    id = id,
    code = code,
    value = value,
)

/** Maps the model to its stored `ChargeTimeTypeRow`. */
fun ChargeTimeType.toEntity(): ChargeTimeTypeRow = ChargeTimeTypeRow(
    id = id,
    code = code,
    value = value,
)

/** Maps the stored row to its `Charges` model. */
fun ChargesRow.toModel(): Charges = Charges(
    resourceType = resourceType,
    resourceId = resourceId,
    id = id,
    clientId = clientId,
    loanId = loanId,
    chargeId = chargeId,
    name = name,
    chargeTimeType = chargeTimeType?.toModel(),
    chargeDueDate = chargeDueDate?.toModel(),
    dueDate = dueDate,
    chargeCalculationType = chargeCalculationType?.toModel(),
    currency = currency?.toModel(),
    amount = amount,
    amountPaid = amountPaid,
    amountWaived = amountWaived,
    amountWrittenOff = amountWrittenOff,
    amountOutstanding = amountOutstanding,
    penalty = penalty,
    active = active,
    paid = paid,
    waived = waived,
)

/** Maps the model to its stored `ChargesRow`. */
fun Charges.toEntity(): ChargesRow = ChargesRow(
    resourceType = resourceType,
    resourceId = resourceId,
    id = id,
    clientId = clientId,
    loanId = loanId,
    chargeId = chargeId,
    name = name,
    chargeTimeType = chargeTimeType?.toEntity(),
    chargeDueDate = chargeDueDate?.toEntity(),
    dueDate = dueDate,
    chargeCalculationType = chargeCalculationType?.toEntity(),
    currency = currency?.toEntity(),
    amount = amount,
    amountPaid = amountPaid,
    amountWaived = amountWaived,
    amountWrittenOff = amountWrittenOff,
    amountOutstanding = amountOutstanding,
    penalty = penalty,
    active = active,
    paid = paid,
    waived = waived,
)

/** Maps the stored row to its `PaymentTypeOption` model. */
fun PaymentTypeOptionRow.toModel(): PaymentTypeOption = PaymentTypeOption(
    id = id,
    name = name,
    description = description,
    isCashPayment = isCashPayment,
    position = position,
)

/** Maps the model to its stored `PaymentTypeOptionRow`. */
fun PaymentTypeOption.toEntity(): PaymentTypeOptionRow = PaymentTypeOptionRow(
    id = id,
    name = name,
    description = description,
    isCashPayment = isCashPayment,
    position = position,
)
