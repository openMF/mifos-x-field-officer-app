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

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.combineScreenStates
import kpt.core.base.store.screen.mapContent
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.center.CenterRepository
import kpt.core.data.checkerinbox.CheckerInboxRepository
import kpt.core.data.collectionsheet.CollectionSheetRepository
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.data.group.GroupRepository
import kpt.core.data.recent.RecentRecordsRepository
import kpt.core.data.serverconfig.ServerConfigRepository
import kpt.core.data.session.SessionRepository
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardScope
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.dashboard.DueTodayLoan
import kpt.core.model.dashboard.LoanPortfolio
import kpt.core.model.dashboard.can
import kpt.core.model.dashboard.canAnyChecker
import kpt.core.model.dashboard.ownedBy
import kpt.core.model.dashboard.portfolioOf
import kpt.core.model.objects.collectionsheets.LoanCollectionSheet
import kpt.core.model.objects.users.User
import kpt.core.model.utils.matching

/**
 * The field officer's day — the dashboard fan-out shape (FEATURE_LAYER.md): one [ScreenState] slot per
 * card, each from its own repository stream, so one slow or failing source never blanks the others.
 * A card or quick action the user has no permission for is never subscribed and never shown.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val sessionRepository: SessionRepository,
    private val dashboardRepository: DashboardRepository,
    private val checkerInboxRepository: CheckerInboxRepository,
    private val collectionSheetRepository: CollectionSheetRepository,
    private val centerRepository: CenterRepository,
    private val groupRepository: GroupRepository,
    private val formDraftRepository: FormDraftRepository,
    private val serverConfigRepository: ServerConfigRepository,
    private val recentRecordsRepository: RecentRecordsRepository,
) : BaseViewModel<HomeUiState, Nothing, HomeAction>(HomeUiState()) {

    /** The live streams behind each card, by source — what that card's retry re-requests. */
    private val cardStreams = HomeCardId.entries.associateWith { mutableMapOf<String, ScreenDataStream<*>>() }

    init {
        // Local and always present — a plain count beside the cards, never a faked ScreenState slot.
        formDraftRepository.observeAll()
            .onEach { drafts -> updateState { copy(draftCount = drafts.size) } }
            .launchIn(viewModelScope)
        // Local and per user, like drafts — a plain list, never a ScreenState slot.
        recentRecordsRepository.recent
            .onEach { recent -> updateState { copy(recent = recent) } }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            val user = sessionRepository.sessionStream(viewModelScope).state
                .filterIsInstance<ScreenState.Content<User>>()
                .first()
                .data
            start(user)
        }
    }

    override fun handleAction(action: HomeAction) {
        when (action) {
            is HomeAction.Retry -> cardStreams.getValue(action.card).values.forEach { it.retry() }
            HomeAction.Refresh -> cardStreams.values.flatMap { it.values }.distinct().forEach { it.retry() }
        }
    }

    fun onRetry(card: HomeCardId) = trySendAction(HomeAction.Retry(card))

    fun onRefresh() = trySendAction(HomeAction.Refresh)

    private fun <S : ScreenDataStream<*>> S.on(vararg cards: HomeCardId, key: String): S =
        also { stream -> cards.forEach { cardStreams.getValue(it)[key] = stream } }

    private fun start(user: User) {
        updateState {
            copy(header = header.copy(name = user.username.orEmpty(), officeName = user.officeName.orEmpty()), quickActions = QuickAction.entries.toList())
        }

        val userStream = dashboardRepository.userStream(user.userId, viewModelScope).on(*HomeCardId.entries.toTypedArray(), key = "user")
        // The staff link scopes the book. Until it answers, wait; if it cannot be read, fall back to the office.
        val scope: Flow<DashboardScope> = userStream.state.mapNotNull { state ->
            when (state) {
                ScreenState.Loading -> null
                is ScreenState.Content -> DashboardScope(user.officeId, state.data.staffId, user.username.orEmpty())
                else -> DashboardScope(user.officeId, null, user.username.orEmpty())
            }
        }.distinctUntilChanged()

        val businessDate = dashboardRepository.businessDateStream(viewModelScope)
            .on(HomeCardId.COLLECTIONS, HomeCardId.MEETINGS, key = "businessDate")

        header(user, userStream, businessDate)

        val centers by lazy { centerRepository.centersStream(viewModelScope) }
        val cards = buildList {
            // Stitch home leads with Today's collections for every officer; a role the server refuses the
            // sheet to sees the card's own error state (03), never a missing section.
            add(HomeCardId.COLLECTIONS to collectionsCard(user, businessDate, scope))
            workQueueCard(user, scope)?.let { add(HomeCardId.WORK_QUEUE to it) }
            if (user.can("READ_CALENDAR") && user.can("READ_CENTER")) add(HomeCardId.MEETINGS to meetingsCard(businessDate, scope, centers))
            portfolioCard(user, scope) { centers }?.let { add(HomeCardId.PORTFOLIO to it) }
        }
        if (cards.isEmpty()) return
        combine(cards.map { (id, flow) -> flow.map { HomeCardSlot(id, it) } }) { it.toList() }
            .onEach { slots -> updateState { copy(cards = slots) } }
            .launchIn(viewModelScope)
    }

    private fun header(user: User, userStream: ScreenDataStream<DashboardUser>, businessDate: ScreenDataStream<String>) {
        combine(
            userStream.state,
            businessDate.state,
            serverConfigRepository.serverConfigStream(viewModelScope).state,
            serverConfigRepository.instancesStream(viewModelScope).state,
        ) { me, date, config, instances ->
            val cfg = (config as? ScreenState.Content)?.data
            val self = (me as? ScreenState.Content)?.data
            HomeHeader(
                name = self?.firstname?.takeIf { it.isNotBlank() } ?: user.username.orEmpty(),
                initials = listOfNotNull(self?.firstname, self?.lastname).filter { it.isNotBlank() }.joinToString("") { it.first().uppercase() }
                    .ifEmpty { user.username.orEmpty().take(1).uppercase() },
                officeName = user.officeName.orEmpty(),
                businessDate = (date as? ScreenState.Content)?.data,
                instanceName = cfg?.let { c -> (instances as? ScreenState.Content)?.data?.matching(c)?.name ?: c.endPoint },
                ownPortfolio = self?.staffId != null,
            )
        }.onEach { h -> updateState { copy(header = h) } }.launchIn(viewModelScope)
    }

    private fun collectionsCard(user: User, businessDate: ScreenDataStream<String>, scope: Flow<DashboardScope>): Flow<ScreenState<HomeCard>> {
        // No collection sheet for this role, but its loans are readable: compute the day from them.
        if (!user.can("READ_COLLECTIONSHEET") && user.can("READ_LOAN")) return collectionsFromLoans(businessDate, scope)
        // Clients with an active loan in arrears — the "overdue" side of today's list. Absent without READ_LOAN.
        val inArrears: Flow<Set<Long>?> = if (user.can("READ_LOAN")) {
            dashboardRepository.loansStream(DashboardRepository.STATUS_ACTIVE, viewModelScope).on(HomeCardId.COLLECTIONS, key = "arrears").state
                .map { state -> (state as? ScreenState.Content)?.data?.filter { it.inArrears }?.mapNotNull { it.clientId }?.toSet() }
        } else {
            flowOf(null)
        }
        return businessDate.state.thenCard { date ->
            scope.flatMapLatest { s ->
                collectionSheetRepository.individualCollectionSheetStream(
                    RequestCollectionSheetPayload(
                        dateFormat = FINERACT_DATE_FORMAT,
                        locale = "en",
                        officeId = s.officeId.toInt(),
                        staffId = s.staffId?.toInt(),
                        transactionDate = fineractDate(date),
                    ),
                    viewModelScope,
                ).on(HomeCardId.COLLECTIONS, key = "sheet").state.combineContent(inArrears) { sheet, arrears, _ -> collections(sheet, arrears) }
            }
        }
    }

    private fun workQueueCard(user: User, scope: Flow<DashboardScope>): Flow<ScreenState<HomeCard>>? {
        val q = HomeCardId.WORK_QUEUE
        val parts = buildList<Flow<ScreenState<Pair<String, Int>>>> {
            if (user.canAnyChecker()) {
                add(checkerInboxRepository.checkerTasksStream(viewModelScope).on(q, key = CHECKER).state.mapContent { t, _ -> CHECKER to t.size })
            }
            if (user.can("READ_LOAN")) {
                for (status in listOf(DashboardRepository.STATUS_SUBMITTED, DashboardRepository.STATUS_APPROVED)) {
                    val loans = dashboardRepository.loansStream(status, viewModelScope).on(q, key = "loans:$status")
                    add(loans.state.combineContent(scope) { list, sc, _ -> "loans:$status" to list.ownedBy(sc).size })
                }
            }
            if (user.can("READ_CLIENT")) {
                add(dashboardRepository.clientCountStream(user.officeId, "pending", viewModelScope).on(q, key = PENDING).state.mapContent { n, _ -> PENDING to n })
            }
            if (user.can("READ_RESCHEDULELOAN")) {
                add(checkerInboxRepository.rescheduleRequestsStream(viewModelScope).on(q, key = RESCHEDULES).state.mapContent { r, _ -> RESCHEDULES to r.size })
            }
        }
        if (parts.isEmpty()) return null
        return combineScreenStates(parts).mapContent { values, _ ->
            val byKey = values.asKeyed()
            HomeCard.WorkQueue(
                checkerTasks = byKey[CHECKER] as? Int,
                awaitingApproval = byKey["loans:${DashboardRepository.STATUS_SUBMITTED}"] as? Int,
                awaitingDisbursal = byKey["loans:${DashboardRepository.STATUS_APPROVED}"] as? Int,
                pendingClients = byKey[PENDING] as? Int,
                pendingReschedules = byKey[RESCHEDULES] as? Int,
            )
        }
    }

    private fun meetingsCard(
        businessDate: ScreenDataStream<String>,
        scope: Flow<DashboardScope>,
        centers: ScreenDataStream<List<Center>>,
    ): Flow<ScreenState<HomeCard>> {
        val m = HomeCardId.MEETINGS
        centers.on(m, key = "centers")
        return businessDate.state.thenCard { date ->
            val today = date.split('-').map { it.toInt() }
            centers.state.combineContent(scope) { list, sc, _ -> list.inScope(sc).mapNotNull { it.id }.take(MAX_MEETING_CENTERS) }
                .thenCard { ids ->
                    if (ids.isEmpty()) {
                        flowOf(ScreenState.Content(HomeCard.Meetings(emptyList())))
                    } else {
                        combineScreenStates(
                            ids.map { id -> centerRepository.centerWithMeetingStream(id, viewModelScope).on(m, key = "center:$id").state },
                        ).mapContent { rows, _ ->
                            HomeCard.Meetings(
                                rows.filterIsInstance<CenterWithAssociations>()
                                    .filter { c -> c.collectionMeetingCalendar.nextTenRecurringDates.any { it == today } }
                                    .map { c -> MeetingToday(centerId = c.id ?: 0, name = c.name.orEmpty(), groups = c.groupMembers.size, time = c.collectionMeetingCalendar.meetingTime.toMeetingTime()) },
                            )
                        }
                    }
                }
        }
    }

    private fun portfolioCard(user: User, scope: Flow<DashboardScope>, centers: () -> ScreenDataStream<List<Center>>): Flow<ScreenState<HomeCard>>? {
        val p = HomeCardId.PORTFOLIO
        val parts = buildList<Flow<ScreenState<Pair<String, Any>>>> {
            if (user.can("READ_LOAN")) {
                val active = dashboardRepository.loansStream(DashboardRepository.STATUS_ACTIVE, viewModelScope).on(p, key = "active")
                add(active.state.combineContent(scope) { list, sc, _ -> LOANS to portfolioOf(if (sc.staffId != null) list.ownedBy(sc) else list) })
            }
            if (user.can("READ_CLIENT")) {
                add(dashboardRepository.clientCountStream(user.officeId, "active", viewModelScope).on(p, key = "activeClients").state.mapContent { n, _ -> CLIENTS to n })
            }
            if (user.can("READ_SAVINGSACCOUNT")) {
                val savings = dashboardRepository.savingsStream(viewModelScope).on(p, key = SAVINGS)
                add(
                    savings.state.combineContent(scope) { list, sc, _ ->
                        val mine = if (sc.staffId != null) list.filter { it.fieldOfficerId == sc.staffId } else list
                        SAVINGS to mine.groupBy { it.currencyCode }.map { (code, rows) -> Money(rows.sumOf { it.balance }, code, rows.first().currencySymbol) }
                    },
                )
            }
            if (user.can("READ_GROUP")) {
                val groups = groupRepository.groupsStream(viewModelScope).on(p, key = GROUPS)
                add(
                    groups.state.combineContent(scope) { list, sc, _ ->
                        GROUPS to list.count { g -> if (sc.staffId != null) g.staffId?.toLong() == sc.staffId else g.officeId?.toLong() == sc.officeId }
                    },
                )
            }
            if (user.can("READ_CENTER")) {
                add(centers().on(p, key = CENTERS).state.combineContent(scope) { list, sc, _ -> CENTERS to list.inScope(sc).size })
            }
        }
        if (parts.isEmpty()) return null
        return combineScreenStates(parts).mapContent { values, _ ->
            val byKey = values.asKeyed()
            val loans = byKey[LOANS] as? LoanPortfolio
            HomeCard.Portfolio(
                activeClients = byKey[CLIENTS] as? Int,
                activeLoans = loans?.activeLoans,
                inArrears = loans?.inArrears,
                loanTotals = loans?.byCurrency,
                savings = (byKey[SAVINGS] as? List<*>)?.filterIsInstance<Money>(),
                groups = byKey[GROUPS] as? Int,
                centers = byKey[CENTERS] as? Int,
            )
        }
    }

    /**
     * Today's collections without READ_COLLECTIONSHEET: the officer's active loans (the portfolio's own
     * scope — by staff link, else office-wide), then each one's repayment schedule for the business date.
     */
    private fun collectionsFromLoans(businessDate: ScreenDataStream<String>, scope: Flow<DashboardScope>): Flow<ScreenState<HomeCard>> {
        val active = dashboardRepository.loansStream(DashboardRepository.STATUS_ACTIVE, viewModelScope).on(HomeCardId.COLLECTIONS, key = "arrears")
        return businessDate.state.thenCard { date ->
            active.state.combineContent(scope) { list, sc, _ -> if (sc.staffId != null) list.ownedBy(sc) else list }.thenCard { owned ->
                dashboardRepository.dueTodayStream(date, owned.map { it.id }, viewModelScope).on(HomeCardId.COLLECTIONS, key = "dueToday")
                    .state.mapContent { rows, _ -> collectionsFrom(rows, owned) }
            }
        }
    }

    private fun collectionsFrom(rows: List<DueTodayLoan>, owned: List<DashboardLoan>): HomeCard {
        val dueRows = rows.filter { it.due > 0.0 }
        val dueClients = dueRows.mapNotNull { it.clientId }.toSet()
        val inArrears = owned.filter { it.inArrears }.mapNotNull { it.clientId }.toSet()
        fun money(list: List<DueTodayLoan>, amount: (DueTodayLoan) -> Double) =
            list.groupBy { it.currencyCode }.map { (code, group) -> Money(group.sumOf(amount), code, group.first().currencySymbol) }
        return HomeCard.Collections(
            clientsDue = dueClients.size,
            loansDue = dueRows.size,
            dueByCurrency = money(dueRows) { it.due },
            collectedByCurrency = money(rows) { it.paid }.filter { it.amount > 0.0 },
            overdueClients = dueClients.count { it in inArrears },
        )
    }

    private fun collections(sheet: IndividualCollectionSheet, inArrears: Set<Long>?): HomeCard {
        val clients = sheet.clients.orEmpty()
        val allLoans = clients.flatMap { it.loans.orEmpty() }
        val dueLoans = allLoans.filter { it.totalDue > 0.0 }
        val dueClients = clients.filter { c -> c.loans.orEmpty().any { it.totalDue > 0.0 } }
        fun money(rows: List<LoanCollectionSheet>, amount: (LoanCollectionSheet) -> Double) =
            rows.groupBy { it.currency?.code.orEmpty() }.map { (code, group) ->
                Money(group.sumOf(amount), code, group.first().currency?.displaySymbol.orEmpty())
            }
        return HomeCard.Collections(
            clientsDue = dueClients.size,
            loansDue = dueLoans.size,
            dueByCurrency = money(dueLoans) { it.totalDue },
            collectedByCurrency = money(allLoans) { (it.principalPaid ?: 0.0) + (it.interestPaid ?: 0.0) + (it.feePaid ?: 0.0) }
                .filter { it.amount > 0.0 },
            overdueClients = inArrears?.let { ids -> dueClients.count { it.clientId.toLong() in ids } },
        )
    }

    private fun List<Center>.inScope(scope: DashboardScope): List<Center> =
        filter { c -> if (scope.staffId != null) c.staffId?.toLong() == scope.staffId else c.officeId?.toLong() == scope.officeId }

    private companion object {
        const val FINERACT_DATE_FORMAT = "dd MMMM yyyy"

        // ponytail: one calendar read per centre — fine for one officer's centres; a server-side
        // "meetings on date" read would replace this if Fineract's meetingDate filter ever works.
        const val MAX_MEETING_CENTERS = 20

        const val CHECKER = "checker"
        const val PENDING = "pendingClients"
        const val RESCHEDULES = "reschedules"
        const val LOANS = "loans"
        const val CLIENTS = "clients"
        const val SAVINGS = "savings"
        const val GROUPS = "groups"
        const val CENTERS = "centers"
    }
}

private val MONTHS = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

/** `2026-10-26` → `26 October 2026`, the `dd MMMM yyyy` form Fineract's collection sheet expects. */
internal fun fineractDate(iso: String): String {
    val (y, m, d) = iso.split('-').map { it.toInt() }
    return "${d.toString().padStart(2, '0')} ${MONTHS[m - 1]} $y"
}

/** Fineract `meetingTime` → `HH:mm`: an `[h, m]` array or an `"HH:mm[:ss]"` string; anything else is unknown. */
internal fun JsonElement?.toMeetingTime(): String? {
    val (h, m) = when (this) {
        is JsonArray -> mapNotNull { it.jsonPrimitive.intOrNull }.takeIf { it.size >= 2 }?.let { it[0] to it[1] }
        is JsonPrimitive -> contentOrNull?.split(':')?.takeIf { it.size >= 2 }?.let { p -> p[0].toIntOrNull()?.let { hh -> p[1].toIntOrNull()?.let { mm -> hh to mm } } }
        else -> null
    } ?: return null
    if (h !in 0..23 || m !in 0..59) return null
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}

/** `combineScreenStates` yields `List<Any?>`; each part is a `key to value` pair. */
private fun List<Any?>.asKeyed(): Map<String, Any?> = filterIsInstance<Pair<*, *>>().associate { it.first.toString() to it.second }

/**
 * Build a card from a dependency's VALUE. [inner] is rebuilt only when that value changes — a refresh
 * re-emits Content with a new fetch time, which must not tear down and re-create the card's streams.
 * Until the dependency is Content, its own state (loading / offline / error) is the card's state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
private fun <T> Flow<ScreenState<T>>.thenCard(inner: (T) -> Flow<ScreenState<HomeCard>>): Flow<ScreenState<HomeCard>> =
    distinctUntilChangedBy { state -> if (state is ScreenState.Content) state.data else state::class }
        .flatMapLatest { state ->
            if (state is ScreenState.Content) {
                inner(state.data)
            } else {
                // mapContent re-types the non-Content state unchanged; its transform never runs here.
                flowOf(state).mapContent<T, HomeCard> { _, _ -> error("unreachable: state is not Content") }
            }
        }
