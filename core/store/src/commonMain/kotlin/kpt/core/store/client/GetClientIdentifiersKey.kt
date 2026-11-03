/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.client

/** Addresses one `getClientIdentifiers` read. */
data class GetClientIdentifiersKey(
    /** The client's server id. */
    val clientId: Long,
    /** The identifier's server id. */
    val identifierId: Long,
)
