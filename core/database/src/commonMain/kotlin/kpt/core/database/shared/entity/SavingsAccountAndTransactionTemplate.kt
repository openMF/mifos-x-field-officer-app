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

import kpt.core.database.savings.entity.SavingsAccountTransactionTemplateEntity
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity

/**
 * Created by Rajan Maurya on 21/08/16.
 */
class SavingsAccountAndTransactionTemplate {
    var savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity? = null
    var savingsAccountTransactionTemplate: SavingsAccountTransactionTemplateEntity? = null

    constructor()
    constructor(
        savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity?,
        savingsAccountTransactionTemplate: SavingsAccountTransactionTemplateEntity?,
    ) {
        this.savingsAccountWithAssociations = savingsAccountWithAssociations
        this.savingsAccountTransactionTemplate = savingsAccountTransactionTemplate
    }

    override fun toString(): String {
        return "SavingsAccountAndTransactionTemplate{" +
            "savingsAccountWithAssociations=" + savingsAccountWithAssociations +
            ", savingsAccountTransactionTemplate=" + savingsAccountTransactionTemplate +
            '}'
    }
}
