/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects

import kotlinx.serialization.Serializable
import kpt.core.model.objects.commonfiles.InterestType

/**
 * Created by ishankhanna on 14/02/14.
 */
@Serializable
data class SearchedEntity(

    var entityId: Int = 0,

    var entityAccountNo: String? = null,

    var entityName: String? = null,

    var entityType: String? = null,

    var parentId: Int = 0,

    var parentName: String? = null,

    var entityStatus: InterestType? = null,

) {
    val description: String
        get() = "#$entityId - $entityName"
}
