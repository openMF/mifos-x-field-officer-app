/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.checkerinbox

/** Addresses one `getCheckerTasksFromResourceId` read. */
data class GetCheckerTasksFromResourceIdKey(
    /** The action the inbox is filtered on. */
    val actionName: String? = null,
    /** The entity kind the inbox is filtered on. */
    val entityName: String? = null,
    /** The id of the resource the records belong to. */
    val resourceId: Int? = null,
)
