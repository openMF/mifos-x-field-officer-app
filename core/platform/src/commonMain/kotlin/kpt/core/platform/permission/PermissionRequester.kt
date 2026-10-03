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
 * A runtime permission, named by what the app wants rather than by any platform's identifier.
 *
 * The Android manifest strings this maps to are an implementation detail of the Android actual —
 * they must not appear in commonMain. The surface this replaces leaked them: its
 * `getRequiredPermissionsForExport()` returned `List<String>` of `Manifest.permission.*` values
 * from common code, which meant every caller was writing Android into a multiplatform module.
 */
enum class Permission {
    Camera,
    Microphone,
    Location,
    Notifications,
    Contacts,

    /**
     * Read access to user media/files. On Android this resolves to `READ_MEDIA_IMAGES` on API 33+
     * and the legacy `READ/WRITE_EXTERNAL_STORAGE` pair below it — the caller does not choose.
     */
    Storage,
}

/**
 * The outcome of a permission request. Every platform returns this same vocabulary so that callers
 * stay platform-agnostic (CORE_PLATFORM.md).
 */
sealed interface PermissionResult {
    /** Granted — the caller may proceed. */
    data object Granted : PermissionResult

    /** Refused, but askable again. The caller should explain why and may re-request. */
    data object Denied : PermissionResult

    /**
     * Refused in a way only the system settings screen can undo. Re-requesting is a no-op that
     * the user never sees, so the caller must offer [PermissionRequester.openAppSettings] instead.
     */
    data object DeniedPermanently : PermissionResult
}

/**
 * Requests runtime permissions.
 *
 * Deliberately Compose-free: `core/platform` applies only `kmp.library.convention` — no Compose
 * compiler plugin — and per CORE_PLATFORM.md an expect/actual *Composable* is a code smell. The
 * dialog that drives this lives once in `core/designsystem` commonMain; only the platform mechanics
 * are split here.
 *
 * Construct it from an [AppContext], which `core-base/platform` already provides on every target
 * and `core/platform` re-exports via `api(projects.coreBase.platform)`.
 *
 * A platform that does not gate a capability returns [PermissionResult.Granted] rather than
 * throwing — the surface this replaces used `TODO()` on desktop, JS, wasmJS and native, which threw
 * `NotImplementedError` at runtime the moment a non-Android target reached it.
 */
expect class PermissionRequester(context: AppContext) {

    /** True when every one of [permissions] is already held. */
    fun isGranted(permissions: List<Permission>): Boolean

    /**
     * Requests [permissions], suspending until the user answers.
     *
     * Returns [PermissionResult.Granted] only when ALL of them were granted; otherwise the
     * strongest refusal among them, so a caller needing camera *and* microphone cannot mistake a
     * partial grant for success.
     */
    suspend fun request(permissions: List<Permission>): PermissionResult

    /** Opens this app's system settings page, for recovering from [PermissionResult.DeniedPermanently]. */
    fun openAppSettings()
}
