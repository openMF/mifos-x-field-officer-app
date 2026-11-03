/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

/** An account. */
data class Account(
    /** The server id of the account. */
    var accountId: String? = null,

    /** The account's Fineract status id. */
    var accountStatusId: Int = 0,

    /** The server id of the loan. */
    var loanId: Int = 0,
)
