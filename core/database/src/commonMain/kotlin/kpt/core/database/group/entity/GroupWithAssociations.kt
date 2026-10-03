/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.entity

import kotlinx.serialization.Serializable
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.model.shared.Timeline

/**
 * Created by ishankhanna on 29/06/14.
 */
@Serializable
data class GroupWithAssociations(
    val id: Int? = null,

    val accountNo: String? = null,

    val name: String? = null,

    val status: ClientStatusEntity? = null,

    val active: Boolean? = null,

    val activationDate: List<Int?> = emptyList(),

    val officeId: Int? = null,

    val officeName: String? = null,

    val staffId: Int? = null,

    val staffName: String? = null,

    val hierarchy: String? = null,

    val groupLevel: Int? = null,

    val clientMembers: List<ClientEntity> = emptyList(),

    val timeline: Timeline? = null,
)
