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
 * Created by mayankjindal on 02/10/16.
 */
@Serializable
data class Group(
    var id: Int? = null,

    var accountNo: Int? = null,

    var name: String? = null,

    var externalId: Int? = null,

    var status: Status? = null,

    var active: Boolean? = null,

    var activationDate: List<Int>? = null,

    var officeId: Int? = null,

    var officeName: String? = null,

    var hierarchy: String? = null,

    var groupLevel: Int? = null,

    var timeline: GroupTimeline? = null,
)
