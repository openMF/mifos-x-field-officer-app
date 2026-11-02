/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.ui

/**
 * Test-tag registry for the home / bottom-nav dashboard.
 *
 * Consumed by:
 *  - `HomeScreen` via `Modifier.testTag(...)`.
 *  - Maestro on-device flow `maestro/screen-state/tab-switch.yaml` — references
 *    the `home_dashboard_scroll` string constant directly.
 */
object TestTags {

    /** Tags for `HomeScreen` (the bottom-nav home dashboard). */
    object Home {
        /**
         * Root [Scaffold] surface of `HomeScreen` — always rendered regardless
         * of dashboard loading state. Stable target for Compose UI tests.
         */
        const val SCREEN: String = "home.screen"

        /** The vertically-scrollable dashboard `Column`. */
        const val DASHBOARD_SCROLL: String = "home_dashboard_scroll"

        /**
         * The field officer dashboard — the Home body supplied through ForkBackboneRegistry.homeBody.
         * Values are the `testTag`s declared in `idea-layer/screens/home/ui.yaml`, which the Maestro
         * flows assert against; change one there, change it here.
         */
        const val DASHBOARD: String = "home_dashboard"
        const val HEADER: String = "home_header"
        const val SCOPE_CHIP: String = "home_scope_chip"
        const val SKELETON: String = "home_skeleton"
        const val COLLECTIONS: String = "home_collections"
        const val COLLECTIONS_HERO: String = "home_collections_hero"
        const val OFFLINE_BANNER: String = "home_offline_banner"
        const val RECENT: String = "home_recent"
        const val RECENT_ROW: String = "home_recent_row"
        const val OPEN_COLLECTION_SHEET: String = "home_open_collection_sheet"
        const val WORK_QUEUE: String = "home_work_queue"
        const val QUEUE_CHECKER: String = "home_queue_checker"
        const val QUEUE_APPROVAL: String = "home_queue_approval"
        const val QUEUE_DISBURSAL: String = "home_queue_disbursal"
        const val QUEUE_PENDING_CLIENTS: String = "home_queue_pending_clients"
        const val QUEUE_RESCHEDULES: String = "home_queue_reschedules"
        const val QUEUE_DRAFTS: String = "home_queue_drafts"
        const val QUEUE_VIEW_ALL: String = "home_queue_view_all"
        const val MEETINGS: String = "home_meetings"
        const val MEETING_ROW: String = "home_meeting_row"
        const val PORTFOLIO: String = "home_portfolio"
        const val QUICK_ACTIONS: String = "home_quick_actions"
        const val QA_CLIENT: String = "home_qa_client"
        const val QA_GROUP: String = "home_qa_group"
        const val QA_CENTER: String = "home_qa_center"
        const val QA_COLLECTION: String = "home_qa_collection"
        const val QA_REPORTS: String = "home_qa_reports"
        const val QA_SEARCH: String = "home_qa_search"
        const val QA_PATH_TRACKER: String = "home_qa_path_tracker"
        const val SETTINGS: String = "home_settings"
        const val PROFILE: String = "home_profile"
        const val SCREEN_ERROR: String = "home_screen_error"
        const val CARD_ERROR: String = "home_card_error"
        const val CARD_ERROR_REASON: String = "home_card_error_reason"
        const val LOAN_QUEUE: String = "home_loan_queue"

        fun loanQueueRow(loanId: Long): String = "home_loan_queue_row_$loanId"

        /** Search — its own destination behind the top bar's magnifier (SearchNavigation). */
        const val SEARCH: String = "home.search"
        const val SEARCH_FIELD: String = "home.search.field"
        const val SEARCH_FILTERS: String = "home.search.filters"
        const val SEARCH_EXACT_MATCH: String = "home.search.exact_match"
        const val SEARCH_RESULTS: String = "home.search.results"

        /**
         * One result row, keyed by its entity id — so a test can assert that a CLIENT row is
         * clickable while a SAVING row, whose destination is not ported, is not.
         */
        fun searchResult(entityId: Int): String = "home.search.result.$entityId"
    }
}
