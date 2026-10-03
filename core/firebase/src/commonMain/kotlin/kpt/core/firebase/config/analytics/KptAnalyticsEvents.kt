/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.firebase.config.analytics

/**
 * CROSS-CUTTING analytics event keys — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Everything here is true of ANY app built on this template: a session begins, a screen is shown, a
 * request succeeds or fails, data syncs, a permission is granted. None of it names a feature.
 *
 * ## Where feature events go — NOT here
 * A feature's events live in its own package, `kpt/core/firebase/<feature>/` (e.g. `loans/`),
 * outside `config/`. In a fork that file is fork-owned; a sync never rewrites it, so its own
 * events survive every template upgrade. Adding a feature constant to THIS file has two costs:
 * the next sync overwrites it, and every unrelated fork inherits a key for a feature it does not ship.
 *
 * This file previously carried CLIENT / GROUP / CENTER / SURVEY / SAVINGS / MEETING / ATTENDANCE /
 * COLLECTION / REPAYMENT keys — a specific lender's domain, in a brand-neutral template. Those moved
 * out: a fork that ships those features declares them in its own feature packages.
 */
object KptEventTypes {
    // Session + authentication — every app has a session, whether or not it has accounts.
    const val LOGIN_SUCCESS = "login_success"
    const val LOGIN_FAILURE = "login_failure"
    const val LOGOUT = "logout"
    const val SESSION_START = "session_start"
    const val SESSION_END = "session_end"
    const val SESSION_TIMEOUT = "session_timeout"
    const val BIOMETRIC_AUTH_SUCCESS = "biometric_auth_success"
    const val BIOMETRIC_AUTH_FAILURE = "biometric_auth_failure"

    // Navigation — screen-to-screen movement, independent of which screens exist.
    const val SCREEN_VIEW = "screen_view"
    const val NAVIGATION = "navigation"
    const val DEEP_LINK_OPENED = "deep_link_opened"
    const val BACK_PRESSED = "back_pressed"

    // Network + API — the transport layer, shared by every feature that calls anything.
    const val API_CALL_SUCCESS = "api_call_success"
    const val API_CALL_FAILURE = "api_call_failure"
    const val NETWORK_UNAVAILABLE = "network_unavailable"
    const val NETWORK_RESTORED = "network_restored"

    // Sync + offline — the offline-first contract in core-base/store applies to every feature.
    const val SYNC_STARTED = "sync_started"
    const val SYNC_COMPLETED = "sync_completed"
    const val SYNC_FAILED = "sync_failed"
    const val SYNC_CONFLICT = "sync_conflict"
    const val OFFLINE_OPERATION_QUEUED = "offline_operation_queued"
    const val OFFLINE_OPERATION_REPLAYED = "offline_operation_replayed"

    // Settings + preferences — the shell, not a feature.
    const val THEME_CHANGED = "theme_changed"
    const val LANGUAGE_CHANGED = "language_changed"
    const val PREFERENCE_CHANGED = "preference_changed"
    const val PERMISSION_GRANTED = "permission_granted"
    const val PERMISSION_DENIED = "permission_denied"

    // Errors + performance — reported the same way whatever raised them.
    const val VALIDATION_ERROR = "validation_error"
    const val UNHANDLED_ERROR = "unhandled_error"
    const val SCREEN_LOAD_TIME = "screen_load_time"
    const val SLOW_FRAME = "slow_frame"

    // Onboarding — first-run flows exist before any feature does.
    const val TUTORIAL_STARTED = "tutorial_started"
    const val TUTORIAL_STEP_COMPLETED = "tutorial_step_completed"
    const val TUTORIAL_SKIPPED = "tutorial_skipped"
    const val TUTORIAL_COMPLETED = "tutorial_completed"
}

/**
 * CROSS-CUTTING parameter keys — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Feature-specific parameters (a loan product id, a watchlist symbol) belong in that feature's
 * the feature's own `kpt/core/firebase/<feature>/` package, for the same reason as the event types above.
 */
object KptParamKeys {
    // Session
    const val LOGIN_METHOD = "login_method"
    const val SESSION_DURATION_MS = "session_duration_ms"

    // Navigation
    const val SCREEN_NAME = "screen_name"
    const val FROM_SCREEN = "from_screen"
    const val TO_SCREEN = "to_screen"
    const val TRIGGER = "trigger"

    // Network / API
    const val ENDPOINT = "endpoint"
    const val HTTP_METHOD = "http_method"
    const val STATUS_CODE = "status_code"
    const val DURATION_MS = "duration_ms"

    // Sync
    const val SYNC_TYPE = "sync_type"
    const val RECORDS_SYNCED = "records_synced"
    const val CONFLICT_STRATEGY = "conflict_strategy"

    // Errors
    const val ERROR_TYPE = "error_type"
    const val FIELD_NAME = "field_name"

    // Settings
    const val PREFERENCE_NAME = "preference_name"
    const val OLD_VALUE = "old_value"
    const val NEW_VALUE = "new_value"
    const val PERMISSION_NAME = "permission_name"

    // Onboarding
    const val TUTORIAL_NAME = "tutorial_name"
    const val STEP_INDEX = "step_index"
}

/**
 * CROSS-CUTTING parameter values — TEMPLATE-OWNED, full-copied by every sync.
 *
 * Only values whose meaning is independent of any feature.
 */
object KptParamValues {
    // Trigger sources
    const val TRIGGER_USER_ACTION = "user_action"
    const val TRIGGER_DEEP_LINK = "deep_link"
    const val TRIGGER_NOTIFICATION = "notification"
    const val TRIGGER_SYSTEM = "system"

    // Sync kinds
    const val SYNC_FULL = "full"
    const val SYNC_INCREMENTAL = "incremental"
    const val SYNC_MANUAL = "manual"

    // Outcomes
    const val RESULT_SUCCESS = "success"
    const val RESULT_FAILURE = "failure"
    const val RESULT_CANCELLED = "cancelled"
}
