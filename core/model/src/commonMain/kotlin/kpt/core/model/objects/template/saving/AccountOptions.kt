/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.saving

import kotlinx.serialization.Serializable
import kpt.core.model.objects.account.loan.InterestType

/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
/**
 * Created by rajan on 13/3/16.
 */
@Serializable
data class AccountOptions(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** The general-ledger code of the account. */
    var glCode: Int? = null,

    /** True when the account is disabled. */
    var disabled: Boolean? = null,

    /** True when manual journal entries may be posted to the account. */
    var manualEntriesAllowed: Boolean? = null,

    /** The record's type. */
    var type: InterestType? = null,

    /** How the GL account is used (header or detail). */
    var usage: InterestType? = null,

    /** The office name indented to show its place in the hierarchy. */
    var nameDecorated: String? = null,

    /** The tag of the GL account. */
    var tagId: TagId? = null,
)
