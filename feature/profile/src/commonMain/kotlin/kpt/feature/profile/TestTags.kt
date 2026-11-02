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

object TestTags {

    /** Tags for [ProfileScreen] and the fork body — the testTags its idea-layer ui.yaml declares. */
    object Profile {
        /** Root scaffold — always rendered regardless of content state. */
        const val SCREEN: String = "profile_screen"

        /** The fork body, supplied through ForkBackboneRegistry.profileBody. */
        const val BODY: String = "profile.body"
        const val USERNAME: String = "profile.username"
        const val SERVER: String = "profile_session"
        const val OFFLINE_BANNER: String = "profile_offline_banner"
        const val IDENTITY: String = "profile_identity"
        const val FULL_NAME: String = "profile_full_name"
        const val STAFF_CHIP: String = "profile_staff_chip"
        const val NAME_ERROR: String = "profile_name_error"
        const val RETRY: String = "profile_retry"
        const val SCOPE: String = "profile_scope"
        const val ROLES: String = "profile_roles"
        const val LIVE: String = "profile_live"
        const val SERVER_SETTINGS: String = "profile_server_settings"
        const val ACTIONS: String = "profile_actions"
        const val CHANGE_PASSWORD: String = "profile_change_password"
        const val SIGN_OUT: String = "profile_sign_out"
        const val PASSWORD_SHEET: String = "profile_password_sheet"
        const val NEW_PASSWORD: String = "profile_new_password"
        const val REPEAT_PASSWORD: String = "profile_repeat_password"
        const val PASSWORD_ERROR: String = "profile_password_error"
        const val PASSWORD_CANCEL: String = "profile_password_cancel"
        const val PASSWORD_SAVE: String = "profile_password_save"
    }
}
