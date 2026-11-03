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
 * Created by ishankhanna on 03/07/14.
 */
@Serializable
data class DocumentType(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** True when the record is active. */
    var active: Boolean? = null,

    /** True when this option must be filled in. */
    var mandatory: Boolean? = null,

    /** A free-text description. */
    var description: String? = null,

    /** The sort position of this option. */
    var position: Int? = null,
)
