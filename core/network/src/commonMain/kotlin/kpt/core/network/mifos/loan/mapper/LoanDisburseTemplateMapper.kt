/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.loan.mapper

import kpt.core.model.objects.account.loan.Currency
import kpt.core.model.objects.account.loan.PaymentType
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.network.mifos.loan.dto.LoanDisburseTemplateDto

/**
 * Wire → domain for the disbursement template.
 *
 * The domain type declares `currency` and `paymentTypeOptions` non-null while the wire type makes
 * both optional, so an absent currency becomes an empty [Currency] rather than widening the domain:
 * the disburse form always has the fields to render, they are simply blank.
 */
fun LoanDisburseTemplateDto.toDomain(): LoanDisburseTemplate = LoanDisburseTemplate(
    netDisbursalAmount = netDisbursalAmount,
    date = date,
    currency = currency?.let {
        Currency(
            code = it.code,
            name = it.name,
            decimalPlaces = it.decimalPlaces,
            inMultiplesOf = it.inMultiplesOf,
            displaySymbol = it.displaySymbol,
            nameCode = it.nameCode,
        )
    } ?: Currency(),
    paymentTypeOptions = paymentTypeOptions.map { PaymentType(id = it.id, name = it.name) },
)
