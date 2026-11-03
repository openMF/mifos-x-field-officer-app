/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.checkerinboxtask

import kotlinx.serialization.Serializable

/** Server template for a checker inbox search: the options its form offers. */
@Serializable
data class CheckerInboxSearchTemplate(
    /** The actions the inbox can be filtered by. */
    val actionNames: List<String> = emptyList(),
    /** The entities the inbox can be filtered by. */
    val entityNames: List<String> = emptyList(),
)
