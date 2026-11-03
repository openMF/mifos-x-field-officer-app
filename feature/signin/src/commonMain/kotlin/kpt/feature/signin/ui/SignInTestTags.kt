/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin.ui

/** Stable handles for the login screen — asserted by UI tests, never shown to a user. */
object SignInTestTags {
    /** The sign-in screen's root. */
    const val SCREEN = "signin:screen"

    /** The username field. */
    const val USERNAME = "signin:username"

    /** The password field. */
    const val PASSWORD = "signin:password"

    /** The demo-account chooser. */
    const val DEMO_CREDENTIALS = "signin:demo-credentials"

    /** The sign-in button. */
    const val SUBMIT = "signin:submit"

    /** The error message under the form. */
    const val ERROR = "signin:error"

    /** The progress shown while signing in. */
    const val PROGRESS = "signin:progress"

    /** Restored in S1 T3 — absent from the authored replacement. */
    const val LOGO = "signin:logo"

    /**
     * The headline + prompt block. The stitch mockup renders TWO lines here (app name, then the
     * prompt); the pre-migration screen rendered only the prompt.
     */
    const val TITLE = "signin:title"

    /**
     * The modal submit overlay (`ui.yaml#components.progress_overlay`, RE-EXTRACT D4).
     *
     * Distinct from [PROGRESS]: the port replaced the login button with an inline spinner, which is
     * what PROGRESS tagged. v1 and the mockup both keep the button and cover the whole form, so the
     * overlay is its own element and needs its own handle.
     */
    const val PROGRESS_OVERLAY = "signin:progress-overlay"

    /**
     * The password visibility toggle. It renders only while the password field has NO error — on
     * error the slot becomes a non-interactive error icon (ui.yaml#password_field.trailing_slot).
     */
    const val PASSWORD_TOGGLE = "signin:password-toggle"

    /**
     * The instance chip: which Fineract host/tenant this sign-in targets.
     * Replaces v1's blind full-width server-config button, whose tag is retired in v2.
     */
    const val INSTANCE_CHIP = "signin:instance-chip"

    /** The legal consent line, pinned at the bottom. Absent in v1. */
    const val CONSENT = "signin:consent"

    /**
     * Biometric unlock. Present ONLY when there is a stored session AND the device can perform it,
     * so a test asserting its absence is asserting a real product rule, not a layout accident.
     */
    const val BIOMETRIC = "signin:biometric"

    /** Warn-before-lock and locked banners. Absent in v1, which had no throttle at all. */
    const val LOCKOUT_NOTICE = "signin:lockout-notice"

    /** Trailing error icons, shown per field when that field's rule failed (D2 / D3). */
    const val USERNAME_ERROR_ICON = "signin:username-error-icon"

    /** The error icon inside the password field. */
    const val PASSWORD_ERROR_ICON = "signin:password-error-icon"
}
