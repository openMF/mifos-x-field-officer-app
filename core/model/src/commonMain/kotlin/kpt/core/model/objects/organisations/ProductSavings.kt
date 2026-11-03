/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.organisations

import kotlinx.serialization.Serializable

/**
 * Created by nellyk on 12/15/2015.
 */
@Serializable
data class ProductSavings(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** The product's short name. */
    var shortName: String? = null,

    /** A free-text description. */
    var description: String? = null,
)
