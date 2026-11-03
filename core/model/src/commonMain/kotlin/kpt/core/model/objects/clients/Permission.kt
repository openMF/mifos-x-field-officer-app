/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 09/02/14.
 */
@Serializable
data class Permission(
    /** The group the permission belongs to. */
    var grouping: String? = null,

    /** The permission's code (e.g. `CREATE_CLIENT`). */
    var code: String? = null,

    /** The entity the task or permission covers (CLIENT, LOAN, …). */
    var entityName: String? = null,

    /** The action the task or permission covers (create, approve, …). */
    var actionName: String? = null,

    /** True when the permission is selected. */
    var isSelected: Boolean = false,
)
