/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects

import kotlinx.serialization.Serializable
import kpt.core.model.objects.commonfiles.InterestType

/**
 * Created by ishankhanna on 14/02/14.
 */
@Serializable
data class SearchedEntity(

    /** The server id of the entity this record belongs to. */
    var entityId: Int = 0,

    /** The account number of the found record. */
    var entityAccountNo: String? = null,

    /** The entity the task or permission covers (CLIENT, LOAN, …). */
    var entityName: String? = null,

    /** The kind of entity this record belongs to (client, group, loan, …). */
    var entityType: String? = null,

    /** The server id of the parent record. */
    var parentId: Int = 0,

    /** The name of the parent record. */
    var parentName: String? = null,

    /** The status of the found record. */
    var entityStatus: InterestType? = null,

) {
    /** A free-text description. */
    val description: String
        get() = "#$entityId - $entityName"
}
