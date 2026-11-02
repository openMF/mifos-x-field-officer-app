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
    val name: String = "",
    /** Avatar initials — first and last name when the user record has both. */
    val initials: String = "",
    val officeName: String = "",
    /** Fineract business date, `yyyy-MM-dd`; null until loaded. */
    val businessDate: String? = null,
    val instanceName: String? = null,
    /** True when the user is linked to a staff record — "My portfolio" vs the office. */
    val ownPortfolio: Boolean = false,
)

/** Which remote card a slot is — also its retry key and its test tag. */
enum class HomeCardId { COLLECTIONS, WORK_QUEUE, MEETINGS, PORTFOLIO }

/** One remote card's payload. Each card owns its own [ScreenState], so one failure never blanks the rest. */
sealed interface HomeCard {
    /**
     * Today's sheet. [dueByCurrency] is what is still to collect (Fineract reports due = amount − paid);
     * [collectedByCurrency] is what was already collected against today's installments, so the day's
     * goal is due + collected. [overdueClients] is null when the user may not read loans (no arrears source).
     */
    data class Collections(
        val clientsDue: Int,
        val loansDue: Int,
        val dueByCurrency: List<Money>,
        val collectedByCurrency: List<Money> = emptyList(),
        val overdueClients: Int? = null,
    ) : HomeCard

    /** A null count means the user may not read that row — it is not rendered at all. */
    data class WorkQueue(
        val checkerTasks: Int?,
        val awaitingApproval: Int?,
        val awaitingDisbursal: Int?,
        val pendingClients: Int?,
        val pendingReschedules: Int?,
    ) : HomeCard {
        /** Remote items waiting on the officer; the local draft count is added at render time. */
        val total: Int get() = listOfNotNull(checkerTasks, awaitingApproval, awaitingDisbursal, pendingClients, pendingReschedules).sum()
    }

    data class Meetings(val today: List<MeetingToday>) : HomeCard

    /** Each field null when the user may not read it. Money is per currency, never summed across. */
    data class Portfolio(
        val activeClients: Int?,
        val activeLoans: Int?,
        val inArrears: Int?,
        val loanTotals: List<CurrencyTotals>?,
        val savings: List<Money>?,
        val groups: Int?,
        val centers: Int?,
    ) : HomeCard
}

data class Money(val amount: Double, val currencyCode: String, val currencySymbol: String)

/** A centre meeting due on the business date; [time] is `HH:mm` when the calendar carries one. */
data class MeetingToday(val centerId: Int, val name: String, val groups: Int, val time: String? = null)

data class HomeCardSlot(val id: HomeCardId, val state: ScreenState<HomeCard>)

/** Standalone creates and tools only — client-owned account creates start from the client profile. */
enum class QuickAction { NEW_CLIENT, NEW_GROUP, NEW_CENTER, COLLECTION_SHEET, RUN_REPORTS, SEARCH, PATH_TRACKER }

data class HomeUiState(
    val header: HomeHeader = HomeHeader(),
    val cards: List<HomeCardSlot> = emptyList(),
    val quickActions: List<QuickAction> = emptyList(),
    /** Unsynced local drafts — read from the device, so always known, never a loading slot. */
    val draftCount: Int = 0,
    /** Recently opened records (local, per user) — newest first; the card hides when empty. */
    val recent: List<RecentRecord> = emptyList(),
)

sealed interface HomeAction {
    data class Retry(val card: HomeCardId) : HomeAction
    data object Refresh : HomeAction
}

/** Where a tap goes; the nav graph owns the destinations. */
sealed interface HomeNavigation {
    data object CollectionSheet : HomeNavigation
    data object CheckerInbox : HomeNavigation
    data class LoanQueue(val status: Int) : HomeNavigation
    data object PendingClients : HomeNavigation
    data object Reschedules : HomeNavigation
    data object Drafts : HomeNavigation
    data object Settings : HomeNavigation
    data object Profile : HomeNavigation
    data class Center(val centerId: Int) : HomeNavigation
    data class Quick(val action: QuickAction) : HomeNavigation
    data class Recent(val kind: RecentKind, val id: Long) : HomeNavigation
}
