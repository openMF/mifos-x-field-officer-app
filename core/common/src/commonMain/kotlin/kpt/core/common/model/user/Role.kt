/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.model.user

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 09/02/14.
 */
@Serializable
data class Role(
    var id: Int = 0,

    var name: String? = null,

    var description: String? = null,
)
