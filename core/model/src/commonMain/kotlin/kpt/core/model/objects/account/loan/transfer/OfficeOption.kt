/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.transfer

import kotlinx.serialization.Serializable
/** One office choice offered by a template. */
@Serializable
data class OfficeOption(
    /** The server id of this record. */
    val id: Int? = null,
    /** The display name. */
    val name: String? = null,
    /** The office name indented to show its place in the hierarchy. */
    val nameDecorated: String? = null,
)
