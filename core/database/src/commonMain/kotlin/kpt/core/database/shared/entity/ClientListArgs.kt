/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.shared.entity

import kpt.core.database.client.entity.ClientEntity
/*
 * Created by Aditya Gupta on 22/7/23.
*/
/** Navigation arguments for the client list. */
data class ClientListArgs(
    /** The clients to show. */
    val clientsList: List<ClientEntity> = emptyList(),

    /** True when the list is shown inside another screen. */
    val isParentFragment: Boolean = false,
)
