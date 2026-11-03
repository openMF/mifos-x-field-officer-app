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

        /** The greeting block at the top of the dashboard. */
        const val HEADER: String = "home_header"

        /** The chip saying whose book the figures cover — "My portfolio" or the office. */
        const val SCOPE_CHIP: String = "home_scope_chip"

        /** The loading skeleton shown before any card has data. */
        const val SKELETON: String = "home_skeleton"

        /** Today's collections card. */
        const val COLLECTIONS: String = "home_collections"

        /** The collections card's per-currency hero figure. */
        const val COLLECTIONS_HERO: String = "home_collections_hero"

        /** The banner shown while the device has no network. */
        const val OFFLINE_BANNER: String = "home_offline_banner"

        /** The recently-opened records card. */
        const val RECENT: String = "home_recent"

        /** One row of the recently-opened records card. */
        const val RECENT_ROW: String = "home_recent_row"

        /** The collections card's "open collection sheet" link. */
        const val OPEN_COLLECTION_SHEET: String = "home_open_collection_sheet"

        /** The work-queue card. */
        const val WORK_QUEUE: String = "home_work_queue"

        /** The work queue's checker-inbox tile. */
        const val QUEUE_CHECKER: String = "home_queue_checker"

        /** The work queue's awaiting-approval tile. */
        const val QUEUE_APPROVAL: String = "home_queue_approval"

        /** The work queue's awaiting-disbursal tile. */
        const val QUEUE_DISBURSAL: String = "home_queue_disbursal"

        /** The work queue's pending-clients tile. */
        const val QUEUE_PENDING_CLIENTS: String = "home_queue_pending_clients"

        /** The work queue's pending-reschedules tile. */
        const val QUEUE_RESCHEDULES: String = "home_queue_reschedules"

        /** The work queue's unsynced-drafts tile. */
        const val QUEUE_DRAFTS: String = "home_queue_drafts"

        /** The work queue's "view all" link to the checker inbox. */
        const val QUEUE_VIEW_ALL: String = "home_queue_view_all"

        /** The meetings-today card. */
        const val MEETINGS: String = "home_meetings"

        /** One centre meeting row in the meetings card. */
        const val MEETING_ROW: String = "home_meeting_row"

        /** The portfolio card. */
        const val PORTFOLIO: String = "home_portfolio"

        /** The quick-actions card. */
        const val QUICK_ACTIONS: String = "home_quick_actions"

        /** The "new client" quick action. */
        const val QA_CLIENT: String = "home_qa_client"

        /** The "new group" quick action. */
        const val QA_GROUP: String = "home_qa_group"

        /** The "new centre" quick action. */
        const val QA_CENTER: String = "home_qa_center"

        /** The "collection sheet" quick action. */
        const val QA_COLLECTION: String = "home_qa_collection"

        /** The "run reports" quick action. */
        const val QA_REPORTS: String = "home_qa_reports"

        /** The "search" quick action. */
        const val QA_SEARCH: String = "home_qa_search"

        /** The "path tracker" quick action. */
        const val QA_PATH_TRACKER: String = "home_qa_path_tracker"

        /** The header's settings button. */
        const val SETTINGS: String = "home_settings"

        /** The header's profile avatar. */
        const val PROFILE: String = "home_profile"

        /** The whole-screen error shown when every card failed. */
        const val SCREEN_ERROR: String = "home_screen_error"

        /** The error banner inside a card whose refresh failed over cached data. */
        const val CARD_ERROR: String = "home_card_error"

        /** The server's reason line inside a card error. */
        const val CARD_ERROR_REASON: String = "home_card_error_reason"

        /** The loan-queue screen's list. */
        const val LOAN_QUEUE: String = "home_loan_queue"

        /** One loan-queue row, keyed by its loan id. */
        fun loanQueueRow(loanId: Long): String = "home_loan_queue_row_$loanId"

        /** Search — its own destination behind the top bar's magnifier (SearchNavigation). */
        const val SEARCH: String = "home.search"

        /** The search query field. */
        const val SEARCH_FIELD: String = "home.search.field"

        /** The row of record-type filter chips. */
        const val SEARCH_FILTERS: String = "home.search.filters"

        /** The exact-match switch. */
        const val SEARCH_EXACT_MATCH: String = "home.search.exact_match"

        /** The search results list. */
        const val SEARCH_RESULTS: String = "home.search.results"

        /**
         * One result row, keyed by its entity id — so a test can assert that a CLIENT row is
         * clickable while a SAVING row, whose destination is not ported, is not.
         */
        fun searchResult(entityId: Int): String = "home.search.result.$entityId"
    }
}
