/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.office

/** Addresses one `retrieveOffices` read. */
data class RetrieveOfficesKey(
    /** True to include records from every office. */
    val includeAllOffices: Boolean? = false,
    /** Field the results are ordered by. */
    val orderBy: String? = null,
    /** Sort direction (`ASC`, `DESC`). */
    val sortOrder: String? = null,
)
