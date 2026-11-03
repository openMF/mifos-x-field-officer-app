/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.noncoreobjects

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 02/07/14.
 */
@Serializable
data class Document(
    /** The server id of this record. */
    var id: Int = 0,

    /** The kind of entity the document is attached to. */
    var parentEntityType: String? = null,

    /** The server id of the entity the document is attached to. */
    var parentEntityId: Int = 0,

    /** The display name. */
    var name: String? = null,

    /** The file's name. */
    var fileName: String? = null,

    /** The file's size, in bytes. */
    var size: Long = 0,

    /** The record's type. */
    var type: String? = null,

    /** A free-text description. */
    var description: String? = null,
)
