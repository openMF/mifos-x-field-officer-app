/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

/*
 * MOVED from core/network 2026-10-21 (S3c).
 *
 * It is a plain serializable value with no Ktor, Ktorfit or engine types in it, and it is the shape
 * a SCREEN consumes — `feature/client` reads these through `ClientRepository`. Leaving it under
 * `core/network/**/dto` meant every feature that renders one had to depend on `core:network`, which
 * is exactly the layering the Clients slice already rejected once. The repository returns domain
 * types; the network module keeps the wire envelopes that genuinely are wire envelopes.
 */

import kotlinx.serialization.Serializable

/** Server response to a client close template request. */
@Serializable
data class ClientCloseTemplateResponse(
    /** True when the closure reasons are for staff. */
    val isStaff: Boolean,
    /** The closure reasons that may be chosen. */
    val narrations: List<Narration>,
)

/** A closure reason offered by the close-client template. */
@Serializable
data class Narration(
    /** The server id of this record. */
    val id: Int,
    /** The display name. */
    val name: String,
    /** The sort position of this option. */
    val position: Int,
    /** A free-text description. */
    val description: String,
    /** True when the record is active. */
    val active: Boolean,
    /** True when this option must be filled in. */
    val mandatory: Boolean,
)
