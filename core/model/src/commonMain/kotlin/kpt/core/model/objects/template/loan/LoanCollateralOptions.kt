/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class LoanCollateralOptions(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** The sort position of this option. */
    var position: Int? = null,

    /** A free-text description. */
    var description: String? = null,

    /** True when the record is active. */
    var isActive: Boolean? = null,
)
