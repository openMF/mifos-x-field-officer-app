/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.dashboard

import kpt.core.base.store.screen.ScreenState
import kpt.core.model.dashboard.CurrencyTotals
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord

/** The greeting block — rendered from the session at once; date and name fill in as they load. */
data class HomeHeader(
    /** The signed-in username the greeting addresses; title-cased when Fineract stores it in capitals. */
    val name: String = "",
    /** Avatar initials — first and last name when the user record has both. */
    val initials: String = "",
    /** The office the user belongs to, first on the meta line. */
    val officeName: String = "",
    /** Fineract business date, `yyyy-MM-dd`; null until loaded. */
    val businessDate: String? = null,
    /** The server instance's display name (from its access point); null until the config loads. */
    val instanceName: String? = null,
    /** True when the user is linked to a staff record — "My portfolio" vs the office. */
    val ownPortfolio: Boolean = false,
)

/** Which remote card a slot is — also its retry key and its test tag. */
enum class HomeCardId {
    /** Today's collection sheet: who is due, how much, and what was already collected. */
    COLLECTIONS,

    /** Items waiting on the officer: checker tasks, approvals, disbursals, pending clients, reschedules. */
    WORK_QUEUE,

    /** Centre meetings that fall on the business date. */
    MEETINGS,

    /** Book totals: clients, loans, arrears, outstanding, savings, groups and centres. */
    PORTFOLIO,
}

/** One remote card's payload. Each card owns its own [ScreenState], so one failure never blanks the rest. */
sealed interface HomeCard {
    /**
     * Today's sheet. [dueByCurrency] is what is still to collect (Fineract reports due = amount − paid);
     * [collectedByCurrency] is what was already collected against today's installments, so the day's
     * goal is due + collected. [overdueClients] is null when the user may not read loans (no arrears source).
     */
    data class Collections(
        /** Clients with an installment due on the business date. */
        val clientsDue: Int,
        /** Loans with an installment due on the business date. */
        val loansDue: Int,
        /** What is still to collect today, per currency. */
        val dueByCurrency: List<Money>,
        /** What was already collected today, per currency. */
        val collectedByCurrency: List<Money> = emptyList(),
        /** Clients in arrears; null when the user may not read loans. */
        val overdueClients: Int? = null,
    ) : HomeCard

    /** A null count means the user may not read that row — it is not rendered at all. */
    data class WorkQueue(
        /** Maker-checker entries waiting in the checker inbox. */
        val checkerTasks: Int?,
        /** Loans submitted and waiting for approval. */
        val awaitingApproval: Int?,
        /** Loans approved and waiting to be disbursed. */
        val awaitingDisbursal: Int?,
        /** Clients created but not yet activated. */
        val pendingClients: Int?,
        /** Loan reschedule requests waiting for a decision. */
        val pendingReschedules: Int?,
    ) : HomeCard {
        /** Remote items waiting on the officer; the local draft count is added at render time. */
        val total: Int get() = listOfNotNull(
            checkerTasks,
            awaitingApproval,
            awaitingDisbursal,
            pendingClients,
            pendingReschedules,
        ).sum()
    }

    /**
     * The centre meetings on the business date.
     *
     * @property today the meetings, in calendar order; empty when nothing is scheduled today
     */
    data class Meetings(val today: List<MeetingToday>) : HomeCard

    /** Each field null when the user may not read it. Money is per currency, never summed across. */
    data class Portfolio(
        /** Active clients in the book. */
        val activeClients: Int?,
        /** Active loans in the book. */
        val activeLoans: Int?,
        /** Active loans with an overdue installment. */
        val inArrears: Int?,
        /** Outstanding, overdue and PAR per currency. */
        val loanTotals: List<CurrencyTotals>?,
        /** Savings balances per currency. */
        val savings: List<Money>?,
        /** Groups in the book. */
        val groups: Int?,
        /** Centres in the book. */
        val centers: Int?,
    ) : HomeCard
}

/**
 * An amount in one currency — the dashboard never adds amounts across currencies.
 *
 * @property amount the value, in the currency's major unit
 * @property currencyCode the ISO 4217 code (`USD`, `KES`)
 * @property currencySymbol the symbol Fineract reports for the currency
 */
data class Money(val amount: Double, val currencyCode: String, val currencySymbol: String)

/**
 * A centre meeting due on the business date; [time] is `HH:mm` when the calendar carries one.
 *
 * @property centerId the centre, for opening it
 * @property name the centre's name
 * @property groups how many groups meet at the centre
 * @property time the meeting time, when the calendar has one
 */
data class MeetingToday(val centerId: Int, val name: String, val groups: Int, val time: String? = null)

/**
 * One dashboard card in its own load state.
 *
 * @property id which card this is
 * @property state the card's own loading / error / content state
 */
data class HomeCardSlot(val id: HomeCardId, val state: ScreenState<HomeCard>)

/** Standalone creates and tools only — client-owned account creates start from the client profile. */
enum class QuickAction {
    /** Start a new client. */
    NEW_CLIENT,

    /** Start a new group. */
    NEW_GROUP,

    /** Start a new centre. */
    NEW_CENTER,

    /** Open today's collection sheet. */
    COLLECTION_SHEET,

    /** Open the reports list. */
    RUN_REPORTS,

    /** Search clients, groups, centres and accounts. */
    SEARCH,

    /** Open the officer's path tracker. */
    PATH_TRACKER,
}

/** Everything the home dashboard renders, as one immutable snapshot. */
data class HomeUiState(
    /** The greeting block. */
    val header: HomeHeader = HomeHeader(),
    /** The remote cards, each in its own load state. */
    val cards: List<HomeCardSlot> = emptyList(),
    /** The quick-action tiles to show, in order. */
    val quickActions: List<QuickAction> = emptyList(),
    /** Unsynced local drafts — read from the device, so always known, never a loading slot. */
    val draftCount: Int = 0,
    /** Recently opened records (local, per user) — newest first; the card hides when empty. */
    val recent: List<RecentRecord> = emptyList(),
)

/** What the user asked the dashboard to do. */
sealed interface HomeAction {
    /**
     * Reload one card after it failed.
     *
     * @property card the card to reload
     */
    data class Retry(val card: HomeCardId) : HomeAction

    /** Pull-to-refresh: reload every card. */
    data object Refresh : HomeAction
}

/** Where a tap goes; the nav graph owns the destinations. */
sealed interface HomeNavigation {
    /** Today's collection sheet. */
    data object CollectionSheet : HomeNavigation

    /** The maker-checker inbox. */
    data object CheckerInbox : HomeNavigation

    /**
     * The loans in one Fineract status.
     *
     * @property status the Fineract loan status code (`100` submitted, `200` approved, …)
     */
    data class LoanQueue(val status: Int) : HomeNavigation

    /** Clients created but not yet activated. */
    data object PendingClients : HomeNavigation

    /** Loan reschedule requests waiting for a decision. */
    data object Reschedules : HomeNavigation

    /** The unsynced drafts on this device. */
    data object Drafts : HomeNavigation

    /** App settings. */
    data object Settings : HomeNavigation

    /** The signed-in user's profile. */
    data object Profile : HomeNavigation

    /**
     * One centre's details.
     *
     * @property centerId the centre to open
     */
    data class Center(val centerId: Int) : HomeNavigation

    /**
     * A quick-action tile's destination.
     *
     * @property action the tile that was tapped
     */
    data class Quick(val action: QuickAction) : HomeNavigation

    /**
     * A recently opened record.
     *
     * @property kind what sort of record it is (client, group, loan, …)
     * @property id the record's server id
     */
    data class Recent(val kind: RecentKind, val id: Long) : HomeNavigation
}
