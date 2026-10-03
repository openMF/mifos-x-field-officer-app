/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
    var id: Int? = null,

    var name: String? = null,

    var glCode: Int? = null,

    var disabled: Boolean? = null,

    var manualEntriesAllowed: Boolean? = null,

    var type: InterestType? = null,

    var usage: InterestType? = null,

    var nameDecorated: String? = null,

    var tagId: TagId? = null,
)
