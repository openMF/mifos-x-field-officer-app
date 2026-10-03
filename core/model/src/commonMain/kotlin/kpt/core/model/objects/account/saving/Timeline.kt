/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.saving

import kotlinx.serialization.Serializable
@Serializable
data class Timeline(
    var submittedOnDate: List<Int?> = ArrayList(),

    var submittedByUsername: String? = null,

    var submittedByFirstname: String? = null,

    var submittedByLastname: String? = null,

    var approvedOnDate: List<Int?> = ArrayList(),

    var approvedByUsername: String? = null,

    var approvedByFirstname: String? = null,

    var approvedByLastname: String? = null,

    var activatedOnDate: List<Int?> = ArrayList(),
)
