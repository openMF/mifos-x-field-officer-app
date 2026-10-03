/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.organisations

import kotlinx.serialization.Serializable

/**
 * Created by nellyk on 2/21/2016.
 */
@Serializable
data class LoanPurpose(
    var id: Int? = null,

    var name: String? = null,

    var position: String? = null,
)
