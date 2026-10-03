/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kpt.core.database.loan.entity.LoanRepaymentTemplateEntity
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations

/**
 *
 * Created by Rajan Maurya on 08/08/16.
 */
class LoanAndLoanRepayment {
    var loanWithAssociations: LoanWithAssociations? = null
    var loanRepaymentTemplate: LoanRepaymentTemplateEntity? = null

    constructor()
    constructor(
        loanWithAssociations: LoanWithAssociations?,
        loanRepaymentTemplate: LoanRepaymentTemplateEntity?,
    ) {
        this.loanWithAssociations = loanWithAssociations
        this.loanRepaymentTemplate = loanRepaymentTemplate
    }

    override fun toString(): String {
        return "LoanAndLoanRepayment{" +
            "loanWithAssociations=" + loanWithAssociations +
            ", loanRepaymentTemplate=" + loanRepaymentTemplate +
            '}'
    }
}
