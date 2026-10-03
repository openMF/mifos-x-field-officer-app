/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.platform.permission

import kpt.core.base.platform.context.AppContext

/**
 * Permission mechanics for every non-Android target (desktop JVM, JS, wasmJS, native).
 *
 * intentional-noop: none of these platforms gates the capabilities in [Permission] behind an
 * app-level runtime grant the way Android does. Desktop file and camera access is governed by the
 * OS at point of use; a browser prompts on the first `getUserMedia`/`geolocation` call, owned by the
 * page, not by us. So the honest answer is [PermissionResult.Granted] — "proceed, the platform will
 * ask if it needs to" — and [openAppSettings] has no destination to open.
 *
 * This is the graceful not-available contract CORE_PLATFORM.md requires. It replaces four
 * `TODO("Not yet implemented")` bodies that threw `NotImplementedError` at runtime, plus two that
 * silently did nothing while claiming to have asked.
 */
actual class PermissionRequester actual constructor(
    @Suppress("UNUSED_PARAMETER") context: AppContext,
) {

    actual fun isGranted(permissions: List<Permission>): Boolean = true

    actual suspend fun request(permissions: List<Permission>): PermissionResult =
        PermissionResult.Granted

    // intentional-noop: no per-app permission settings surface exists on these targets.
    actual fun openAppSettings() = Unit
}
