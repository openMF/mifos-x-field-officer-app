/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.group

import kotlinx.serialization.Serializable
import kpt.core.model.client.Client
import kpt.core.model.client.ClientStatus
import kpt.core.model.shared.Timeline

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupWithAssociations`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class GroupWithAssociations(
    val id: Int? = null,
    val accountNo: String? = null,
    val name: String? = null,
    val status: ClientStatus? = null,
    val active: Boolean? = null,
    val activationDate: List<Int?> = emptyList(),
    val officeId: Int? = null,
    val officeName: String? = null,
    val staffId: Int? = null,
    val staffName: String? = null,
    val hierarchy: String? = null,
    val groupLevel: Int? = null,
    val clientMembers: List<Client> = emptyList(),
    val timeline: Timeline? = null,
)
