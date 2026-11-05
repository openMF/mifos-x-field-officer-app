/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

/**
 * Append-only test-tag registry for the settings feature.
 * Consumed by Compose UI tests in `feature/settings/src/commonTest/`.
 * APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).
 */
object TestTags {

    /** Tags for [SettingsScreen]. */
    object Settings {
        /** The settings row that opens About. */
        const val ABOUT_ROW: String = "settings_about_row"

        /** The settings row that opens offline sync. */
        const val OFFLINE_SYNC_ROW: String = "settings_offline_sync_row"

        /** The Privacy row that opens [PrivacySheet]. */
        const val PRIVACY_ROW: String = "settings_privacy_row"

        /** Root scaffold — always rendered regardless of dialog state. */
        const val SCREEN: String = "settings_screen"
    }

    /** Tags for [PrivacySheet]. */
    object Privacy {
        /** The bottom sheet root. */
        const val SHEET: String = "privacy_sheet"

        /** The usage-analytics consent switch row. */
        const val ANALYTICS_SWITCH: String = "privacy_analytics_switch"

        /** The crash-reporting consent switch row. */
        const val CRASH_REPORTS_SWITCH: String = "privacy_crash_reports_switch"

        /** The link that opens the privacy policy in the in-app browser. */
        const val POLICY_LINK: String = "privacy_policy_link"
    }

    /** Tags for [NotificationScreen]. */
    object Notification {
        /** Root scaffold — always rendered regardless of content state. */
        const val SCREEN: String = "notification_screen"
    }

    /** Tags for [SyncAndDraftsScreen]. */
    object SyncAndDrafts {
        /** Root scaffold — always rendered regardless of content state. */
        const val SCREEN: String = "sync_and_drafts_screen"

        /** The manual "Prune expired" button (rendered only when there is content). */
        const val PRUNE: String = "sync_and_drafts_prune"

        /** The control that opens the sync and drafts screen. */
        const val OPEN: String = "sync_and_drafts_open"
    }
}
