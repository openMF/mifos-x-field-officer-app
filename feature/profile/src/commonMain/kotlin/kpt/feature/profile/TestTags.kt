/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

/** Test-tag registry for the profile screen; the Compose UI tests and Maestro flows select by these. */
object TestTags {

    /** Tags for [ProfileScreen] and the fork body — the testTags its idea-layer ui.yaml declares. */
    object Profile {
        /** Root scaffold — always rendered regardless of content state. */
        const val SCREEN: String = "profile_screen"

        /** The fork body, supplied through ForkBackboneRegistry.profileBody. */
        const val BODY: String = "profile.body"

        /** The username line in the identity card. */
        const val USERNAME: String = "profile.username"

        /** The server-session card. */
        const val SERVER: String = "profile_session"

        /** The banner shown while the device has no network. */
        const val OFFLINE_BANNER: String = "profile_offline_banner"

        /** The identity card. */
        const val IDENTITY: String = "profile_identity"

        /** The user's full name. */
        const val FULL_NAME: String = "profile_full_name"

        /** The chip naming the user's staff role. */
        const val STAFF_CHIP: String = "profile_staff_chip"

        /** The error shown when the user record failed to load. */
        const val NAME_ERROR: String = "profile_name_error"

        /** The retry button beside the name error. */
        const val RETRY: String = "profile_retry"

        /** The line saying whose book the user works on. */
        const val SCOPE: String = "profile_scope"

        /** The user's role chips. */
        const val ROLES: String = "profile_roles"

        /** The "Live" badge on a published instance. */
        const val LIVE: String = "profile_live"

        /** The link to the server settings. */
        const val SERVER_SETTINGS: String = "profile_server_settings"

        /** The account actions card. */
        const val ACTIONS: String = "profile_actions"

        /** The "change password" action. */
        const val CHANGE_PASSWORD: String = "profile_change_password"

        /** The "sign out" action. */
        const val SIGN_OUT: String = "profile_sign_out"

        /** The change-password sheet. */
        const val PASSWORD_SHEET: String = "profile_password_sheet"

        /** The new-password field. */
        const val NEW_PASSWORD: String = "profile_new_password"

        /** The repeat-password field. */
        const val REPEAT_PASSWORD: String = "profile_repeat_password"

        /** The error shown under the password fields. */
        const val PASSWORD_ERROR: String = "profile_password_error"

        /** The password sheet's cancel button. */
        const val PASSWORD_CANCEL: String = "profile_password_cancel"

        /** The password sheet's save button. */
        const val PASSWORD_SAVE: String = "profile_password_save"
    }
}
