/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin

/** Stable handles for the login screen — asserted by UI tests, never shown to a user. */
object SignInTestTags {
    const val SCREEN = "signin:screen"
    const val USERNAME = "signin:username"
    const val PASSWORD = "signin:password"
    const val SUBMIT = "signin:submit"
    const val ERROR = "signin:error"
    const val PROGRESS = "signin:progress"

    /** Restored in S1 T3 — absent from the authored replacement. */
    const val LOGO = "signin:logo"

    /** The server-config entry point. Without it a first-run user can never sign in. */
    const val SERVER_CONFIG = "signin:server-config"

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

    /** Trailing error icons, shown per field when that field's rule failed (D2 / D3). */
    const val USERNAME_ERROR_ICON = "signin:username-error-icon"
    const val PASSWORD_ERROR_ICON = "signin:password-error-icon"
}
