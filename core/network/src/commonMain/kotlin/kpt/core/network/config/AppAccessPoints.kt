/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.config

import kpt.core.base.network.AccessPoint
import kpt.core.base.network.AccessPointKind
import kpt.core.base.network.AuthScheme
import kpt.core.base.network.HeaderSpec

/**
 * The per-fork list of network access points this app talks to — REST and Supabase in ONE place.
 *
 * **SoT: `app-profile/app.yaml#network.access_points`.** A fork declares its N typed endpoints there and
 * runs `./gradlew syncForkConfig`, which regenerates the sentinel-bounded [points] block below (do not
 * hand-edit it). The framework-owned `AccessPointRegistry` (core-base/network) wraps this list — the
 * `NetworkModule` binds `single { AccessPointRegistry(AppAccessPoints.points) }` — and the `restApi("<id>")`
 * DSL resolves transports from it, so a fork only writes API interfaces + one `restApi("<id>")` line each.
 *
 * The default entries are real `https://` URLs (no `YOUR_` placeholder) so the Supabase point is
 * default-configured, not inert. A fork replaces these via app-profile + syncForkConfig.
 */
object AppAccessPoints {
    // syncForkConfig:access-points:begin — GENERATED from app-profile/app.yaml#network.access_points.
    // Edit access points THERE (the SoT) and run `./gradlew syncForkConfig`; do not hand-edit this block.
    // `type` defaults to UrlType(id.uppercase()) — value-class-equal to the AppUrlTypes.* constants.
    val points: List<AccessPoint> = listOf(
        AccessPoint(
            id = "mifos",
            kind = AccessPointKind.REST,
            baseUrl = "https://apis.mifos.community/",
            basePath = "1.0/field/v1/",
            loggableHost = "apis.mifos.community",
            auth = AuthScheme.BASIC,
            headers = listOf(
                HeaderSpec(name = "Fineract-Platform-TenantId", value = "mifos-bank-1"),
                HeaderSpec(name = "Content-Type", value = "application/json"),
                HeaderSpec(name = "Accept", value = "application/json"),
                HeaderSpec(name = "Authorization", runtimeKey = "mifos.auth"),
            ),
        ),
        AccessPoint(
            id = "staging",
            kind = AccessPointKind.REST,
            baseUrl = "https://demo.mifos.org/",
            basePath = "fineract-provider/api/v1/",
            loggableHost = "demo.mifos.org",
            auth = AuthScheme.BASIC,
            headers = listOf(
                HeaderSpec(name = "Fineract-Platform-TenantId", value = "default"),
                HeaderSpec(name = "Content-Type", value = "application/json"),
                HeaderSpec(name = "Accept", value = "application/json"),
                HeaderSpec(name = "Authorization", runtimeKey = "staging.auth"),
            ),
        ),
    )
    // syncForkConfig:access-points:end
}
