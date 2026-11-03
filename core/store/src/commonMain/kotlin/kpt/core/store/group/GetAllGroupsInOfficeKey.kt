/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.group

/** Addresses one `getAllGroupsInOffice` read. */
data class GetAllGroupsInOfficeKey(
    /** The office's server id. */
    val officeId: Int,
    /** Extra query parameters sent with the request. */
    val params: Map<String, String>,
)
