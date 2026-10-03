/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.firebase.config.crashlytics

/**
 * CROSS-CUTTING crash context — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Mirrors its `config/analytics/` sibling: this file knows about NO feature. A feature's crash context
 * lives with its analytics in `kpt/core/firebase/<feature>/`, outside `config/` — fork-owned in a
 * fork; the shipped `loans/` is the template's showcase of that layout.
 *
 * ## What this is for
 * A stack trace says where a crash happened; it rarely says what the user was doing. These helpers
 * attach the breadcrumbs that make a report actionable — which screen, which sync, which request —
 * through the kmptoolkit [CrashReporter] contract (`recordException` / `log` / `setCustomKey`).
 *
 * ## The rule these helpers exist to enforce
 * A crash report leaves the device and is readable by anyone with console access, so **no key set
 * here carries a value the user typed, an amount, or an identifier that resolves to a person**.
 * Screen names, request paths and durations describe the app; account numbers describe the user.
 * Feature packages inherit the same rule — see `loans/` for how it is applied.
 */
object KptCrashKeys {
    const val CURRENT_SCREEN = "current_screen"
    const val PREVIOUS_SCREEN = "previous_screen"
    const val SESSION_ID = "session_id"
    const val NETWORK_STATE = "network_state"
    const val SYNC_IN_FLIGHT = "sync_in_flight"
    const val LAST_ENDPOINT = "last_endpoint"
    const val LAST_STATUS_CODE = "last_status_code"
    const val APP_LOCALE = "app_locale"
    const val THEME_MODE = "theme_mode"
    const val PENDING_WRITES = "pending_writes"
}
