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

@Serializable
data class ClientCloseTemplateResponse(
    val isStaff: Boolean,
    val narrations: List<Narration>,
)

@Serializable
data class Narration(
    val id: Int,
    val name: String,
    val position: Int,
    val description: String,
    val active: Boolean,
    val mandatory: Boolean,
)
