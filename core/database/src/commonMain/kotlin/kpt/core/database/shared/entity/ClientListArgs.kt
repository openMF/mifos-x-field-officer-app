/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kpt.core.database.client.entity.ClientEntity
/*
 * Created by Aditya Gupta on 22/7/23.
*/
data class ClientListArgs(
    val clientsList: List<ClientEntity> = emptyList(),

    val isParentFragment: Boolean = false,
)
