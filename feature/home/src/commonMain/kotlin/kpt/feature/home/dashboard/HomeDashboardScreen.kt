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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.DomainAdd
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.designsystem.component.KptShimmerLoadingBox
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.dashboard.IndependentCardLayout
import kpt.core.base.ui.dashboard.toDashboardProgressState
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AccentColors
import kpt.core.designsystem.theme.finance
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord
import kpt.core.store.config.rememberAppErrorMessageFor
import kpt.core.ui.util.exposeTestTagsAsResourceIds
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_card_cached
import kpt.feature.home.generated.resources.feature_home_dashboard_card_error
import kpt.feature.home.generated.resources.feature_home_dashboard_card_error_body
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_clients_value
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_collected
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_collected_today
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_due_amount
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_due_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_empty
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_goal
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_of_goal
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_open
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_overdue
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_overdue_chip
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_percent_collected
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_remains
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_summary
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_title
import kpt.feature.home.generated.resources.feature_home_dashboard_error_body
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_busy
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_failed
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_forbidden
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_not_found
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_offline
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_rejected
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_server
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_signed_out
import kpt.feature.home.generated.resources.feature_home_dashboard_error_kind_timeout
import kpt.feature.home.generated.resources.feature_home_dashboard_error_title
import kpt.feature.home.generated.resources.feature_home_dashboard_greeting_afternoon
import kpt.feature.home.generated.resources.feature_home_dashboard_greeting_evening
import kpt.feature.home.generated.resources.feature_home_dashboard_greeting_morning
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_center_groups
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_on_schedule
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_today
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_upcoming
import kpt.feature.home.generated.resources.feature_home_dashboard_meetings_empty
import kpt.feature.home.generated.resources.feature_home_dashboard_meetings_summary
import kpt.feature.home.generated.resources.feature_home_dashboard_meetings_title
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_banner_body
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_banner_title
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_body
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_enabled
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_label
import kpt.feature.home.generated.resources.feature_home_dashboard_portfolio_title
import kpt.feature.home.generated.resources.feature_home_dashboard_portfolio_total_outstanding
import kpt.feature.home.generated.resources.feature_home_dashboard_profile
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_collection_sheet
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_new_center
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_new_client
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_new_group
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_path_tracker
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_run_reports
import kpt.feature.home.generated.resources.feature_home_dashboard_qa_search
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_approval
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_checker
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_disbursal
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_drafts
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_pending_badge
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_pending_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_reschedules
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_title
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_view_all
import kpt.feature.home.generated.resources.feature_home_dashboard_quick_actions_available
import kpt.feature.home.generated.resources.feature_home_dashboard_quick_actions_title
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_cached
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_center
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_client
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_group
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_kind_loan
import kpt.feature.home.generated.resources.feature_home_dashboard_recent_title
import kpt.feature.home.generated.resources.feature_home_dashboard_retry
import kpt.feature.home.generated.resources.feature_home_dashboard_retry_sync
import kpt.feature.home.generated.resources.feature_home_dashboard_settings
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_active_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_active_loans
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_collected
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_outstanding
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_overdue
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_par
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_active_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_active_loans
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_centers
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_groups
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_in_arrears
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_overdue
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_par
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_par_target
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_savings
import kpt.feature.home.generated.resources.feature_home_dashboard_time_days_ago
import kpt.feature.home.generated.resources.feature_home_dashboard_time_hours_ago
import kpt.feature.home.generated.resources.feature_home_dashboard_time_just_now
import kpt.feature.home.generated.resources.feature_home_dashboard_time_minutes_ago
import kpt.feature.home.generated.resources.feature_home_dashboard_time_yesterday
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The Home tab body, built from the Stitch markup — one composition per state, each from its own
 * `idea-layer/mockups/home/stitch/0N-home-<state>/code.html`:
 *
 *   01 loading · 02 content · 03 partial_failure · 04 error · 05 offline
 *
 * Measurements are the HTML's Tailwind units, read through [tw] (one unit = `spacing.xs` = 4 dp),
 * so `p-3.5` is `3.5f.tw` and `w-11` is `11.tw`; colours are the theme slots the Tailwind
 * names map to (indigo-50 → primaryContainer, emerald → secondary, amber → tertiary, rose → the
 * finance negative pair, slate-100 → surfaceContainer). Every tap leaves through [onNavigate].
 */
@Composable
fun HomeDashboardScreen(
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    HomeDashboardScreenContent(
        state = state,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        onNavigate = onNavigate,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreenContent(
    state: HomeUiState,
    onRetry: (HomeCardId) -> Unit,
    onRefresh: () -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val states = state.cards.map { it.state }
    val progress = states.toDashboardProgressState()
    val allLoading = states.isNotEmpty() && states.all { it == ScreenState.Loading }
    val allFailed = states.isNotEmpty() && states.all { it is ScreenState.Error || it is ScreenState.NoNetwork }
    val offline = states.any { it is ScreenState.NoNetwork }

    PullToRefreshBox(
        isRefreshing = progress.isAnyLoading && progress.oldestFetchedAt != null,
        onRefresh = onRefresh,
        // Expose testTags as Android resource ids so the Maestro flows (ui.yaml testTags) can find them.
        modifier = modifier.fillMaxSize().exposeTestTagsAsResourceIds().testTag(TestTags.Home.DASHBOARD),
    ) {
        // main: px-4 · 01 pt-3.5 space-y-4 · 02 pt-3 space-y-6 · 04 pt-4 gap-6 · 05 p-4 space-y-4
        val gap = if (allLoading || (allFailed && offline)) 4.tw else 6.tw
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 4.tw, end = 4.tw, top = 3.tw, bottom = 8.tw)
                .testTag(TestTags.Home.DASHBOARD_SCROLL),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            when {
                allLoading -> {
                    Header(state.header, HeaderStyle.Loading, onSettings = { onNavigate(HomeNavigation.Settings) }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    DashboardSkeleton()
                }
                allFailed && offline -> {
                    Header(state.header, HeaderStyle.Offline, onSettings = { onNavigate(HomeNavigation.Settings) }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    OfflineError(onRetryAll = onRefresh)
                    OfflineBanner()
                    QuickActions(state.quickActions, onNavigate, QuickStyle.Offline)
                    RecentCard(state.recent, onNavigate, RecentStyle.Offline)
                }
                allFailed -> {
                    Header(state.header, HeaderStyle.Error, onSettings = { onNavigate(HomeNavigation.Settings) }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    QuickActions(state.quickActions, onNavigate, QuickStyle.Error)
                    RecentCard(state.recent, onNavigate, RecentStyle.Error)
                    ScreenError(onRetryAll = onRefresh)
                }
                else -> {
                    Header(state.header, HeaderStyle.Content, onSettings = { onNavigate(HomeNavigation.Settings) }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    ContentState(state, states, onRetry, onNavigate)
                }
            }
        }
    }
}

/** 02 content · 03 partial_failure — every card owns its state, so one failure never blanks the rest. */
@Composable
private fun ContentState(
    state: HomeUiState,
    states: List<ScreenState<HomeCard>>,
    onRetry: (HomeCardId) -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.tw)) {
        if (states.isNotEmpty()) {
            IndependentCardLayout(
                states = states,
                onRetry = { index -> onRetry(state.cards[index].id) },
                cardChrome = { index, card ->
                    Section(state.cards[index], state.draftCount, state.header.businessDate, onNavigate, { onRetry(state.cards[index].id) }, card)
                },
                loading = { index -> CardSkeleton(state.cards[index].id) },
                error = { index, error -> CardError(onRetry = { onRetry(state.cards[index].id) }, error = error) },
                noNetwork = { index, _ -> CardError(onRetry = { onRetry(state.cards[index].id) }, offline = true) },
            ) { _, data, _ ->
                when (data) {
                    is HomeCard.Collections -> CollectionsBody(data)
                    is HomeCard.WorkQueue -> WorkQueueBody(data, state.draftCount, onNavigate)
                    is HomeCard.Meetings -> MeetingsBody(data, onNavigate)
                    is HomeCard.Portfolio -> PortfolioBody(data)
                }
            }
        }
        QuickActions(state.quickActions, onNavigate, QuickStyle.Content)
        RecentCard(state.recent, onNavigate, RecentStyle.Content)
    }
}

// ── Header (#header) ───────────────────────────────────────────────────────────────────────────

/** "Mon 26 Oct 2026" — English names, as every date in the app is today (DateHelper). */
private val headerDate = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
}

/** "Oct 26" — the meetings header. */
private val shortDate = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    day()
}

private fun String.headerDate(): String = runCatching { headerDate.format(LocalDate.parse(this)) }.getOrDefault(this)
private fun String.shortDate(): String = runCatching { shortDate.format(LocalDate.parse(this)) }.getOrDefault(this)

private enum class HeaderStyle { Loading, Content, Error, Offline }

@Composable
private fun Header(header: HomeHeader, style: HeaderStyle, onSettings: () -> Unit, onProfile: () -> Unit) {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when {
        hour < NOON -> Res.string.feature_home_dashboard_greeting_morning
        hour < EVENING -> Res.string.feature_home_dashboard_greeting_afternoon
        else -> Res.string.feature_home_dashboard_greeting_evening
    }
    val c = KptTheme.colorScheme
    val parts = listOfNotNull(
        header.officeName.takeIf { it.isNotBlank() },
        header.businessDate?.headerDate(),
        header.instanceName?.takeUnless { style == HeaderStyle.Offline || it.isBlank() },
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 1.tw).testTag(TestTags.Home.HEADER),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(if (style == HeaderStyle.Loading) 1.5f.tw else 1.tw)) {
            Text(
                text = stringResource(greeting, header.name.toNameCase()),
                // 01 text-xl bold · 02/04 text-2xl bold · 05 text-2xl extrabold
                style = if (style == HeaderStyle.Loading) KptTheme.typography.titleLarge else KptTheme.typography.headlineSmall,
                fontWeight = if (style == HeaderStyle.Offline) FontWeight.ExtraBold else FontWeight.Bold,
                color = c.onSurface,
                letterSpacing = TIGHT,
            )
            when (style) {
                // 01: one line, text-[11px] medium
                HeaderStyle.Loading -> Text(
                    parts.joinToString(META_SEPARATOR),
                    style = KptTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = c.onSurfaceVariant,
                )
                // 02: text-xs medium, parts split by 1-unit slate dots
                HeaderStyle.Content -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.tw)) {
                    parts.forEachIndexed { i, part ->
                        if (i > 0) Box(Modifier.size(1.tw).background(c.outlineVariant, CircleShape))
                        Text(
                            part,
                            style = KptTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = c.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (i == parts.lastIndex) Modifier.weight(1f, fill = false) else Modifier,
                        )
                    }
                }
                // 04: a cell-tower glyph in amber leads the meta line
                HeaderStyle.Error -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                    Icon(Icons.Outlined.CellTower, contentDescription = null, tint = c.tertiary, modifier = Modifier.size(3.5f.tw))
                    Text(parts.joinToString(META_SEPARATOR), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = c.onSurfaceVariant)
                }
                // 05: "· Offline" in rose semibold where the instance name would be
                HeaderStyle.Offline -> {
                    val offlineLabel = stringResource(Res.string.feature_home_dashboard_offline_label)
                    val negative = MaterialTheme.finance.moneyNegative
                    Text(
                        buildAnnotatedString {
                            append(parts.joinToString(META_SEPARATOR))
                            append(META_SEPARATOR)
                            withStyle(SpanStyle(color = negative, fontWeight = FontWeight.SemiBold)) { append(offlineLabel) }
                        },
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = c.onSurfaceVariant,
                    )
                }
            }
        }
        val profileLabel = stringResource(Res.string.feature_home_dashboard_profile)
        when (style) {
            // 01: the avatar shimmers like everything else that hasn't loaded
            HeaderStyle.Loading -> KptShimmerLoadingBox(
                Modifier.size(11.tw).border(HAIRLINE, c.surfaceContainerHigh, CircleShape),
                shape = CircleShape,
            )
            // 02: w-11 h-11 primary-container, ring-2 white, shadow-card
            HeaderStyle.Content -> Box(
                modifier = Modifier
                    .size(11.tw)
                    .shadow(CARD_SHADOW, CircleShape)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onProfile)
                    .semantics { contentDescription = profileLabel }
                    .testTag(TestTags.Home.PROFILE)
                    .background(c.primaryContainer, CircleShape)
                    .border(0.5f.tw, c.surfaceContainerLowest, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(header.initials.ifEmpty { header.name.take(1).uppercase() }, style = KptTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.primary)
            }
            // 04 / 05 carry no avatar
            else -> Unit
        }
        // Settings sits inline with the greeting — the app has no drawer. Hidden while loading.
        if (style != HeaderStyle.Loading) {
            IconButton(onClick = onSettings, modifier = Modifier.testTag(TestTags.Home.SETTINGS)) {
                Icon(AppIcons.Settings, contentDescription = stringResource(Res.string.feature_home_dashboard_settings), tint = c.onSurfaceVariant)
            }
        }
    }
}

/** Fineract usernames are often upper-case ("IRVING"); the greeting reads them as a name. */
private fun String.toNameCase(): String =
    if (this == uppercase()) lowercase().split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) } else this

// ── Section chrome (02: h2 text-lg bold · summary text-xs · trailing action) ───────────────────

private val HomeCardId.title: StringResource
    get() = when (this) {
        HomeCardId.COLLECTIONS -> Res.string.feature_home_dashboard_collections_title
        HomeCardId.WORK_QUEUE -> Res.string.feature_home_dashboard_queue_title
        HomeCardId.MEETINGS -> Res.string.feature_home_dashboard_meetings_title
        HomeCardId.PORTFOLIO -> Res.string.feature_home_dashboard_portfolio_title
    }

private val HomeCardId.tag: String
    get() = when (this) {
        HomeCardId.COLLECTIONS -> TestTags.Home.COLLECTIONS
        HomeCardId.WORK_QUEUE -> TestTags.Home.WORK_QUEUE
        HomeCardId.MEETINGS -> TestTags.Home.MEETINGS
        HomeCardId.PORTFOLIO -> TestTags.Home.PORTFOLIO
    }

@Composable
private fun Section(
    slot: HomeCardSlot,
    draftCount: Int,
    businessDate: String?,
    onNavigate: (HomeNavigation) -> Unit,
    onRetry: () -> Unit,
    card: @Composable () -> Unit,
) {
    val c = KptTheme.colorScheme
    val content = slot.state as? ScreenState.Content
    val data = content?.data
    // Error/NoNetwork = nothing to show. Content carrying a lastError = the refresh failed over a
    // cache (03: header retry line, the error banner, then the cached figures underneath).
    val failedOverCache = content?.freshnessSignal?.lastError != null
    val failed = slot.state is ScreenState.Error || slot.state is ScreenState.NoNetwork || failedOverCache
    val carded = slot.id == HomeCardId.PORTFOLIO
    SectionCard(carded, Modifier.fillMaxWidth().testTag(slot.id.tag)) {
        Row(verticalAlignment = if (slot.id == HomeCardId.COLLECTIONS) Alignment.Top else Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                    Text(stringResource(slot.id.title), style = KptTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.onSurface)
                    if (data is HomeCard.WorkQueue && data.total + draftCount > 0) {
                        Chip(
                            stringResource(Res.string.feature_home_dashboard_queue_pending_badge, data.total + draftCount),
                            container = c.tertiaryContainer,
                            content = c.onTertiaryContainer,
                            style = KptTheme.typography.labelMedium,
                        )
                    }
                }
                when {
                    failed -> Unit
                    slot.id != HomeCardId.MEETINGS && slot.id != HomeCardId.WORK_QUEUE -> sectionSummary(data)?.let {
                        Text(it, style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant, modifier = Modifier.padding(top = 1.tw))
                    }
                }
            }
            when {
                // In every state the card shows — the sheet opens even when this summary failed to load.
                slot.id == HomeCardId.COLLECTIONS -> LinkAction(
                    stringResource(Res.string.feature_home_dashboard_collections_open),
                    chevron = true,
                    modifier = Modifier.testTag(TestTags.Home.OPEN_COLLECTION_SHEET),
                ) { onNavigate(HomeNavigation.CollectionSheet) }
                slot.id == HomeCardId.WORK_QUEUE && data is HomeCard.WorkQueue && data.checkerTasks != null -> LinkAction(
                    stringResource(Res.string.feature_home_dashboard_queue_view_all),
                    modifier = Modifier.testTag(TestTags.Home.QUEUE_VIEW_ALL),
                ) { onNavigate(HomeNavigation.CheckerInbox) }
                slot.id == HomeCardId.MEETINGS && data is HomeCard.Meetings && data.today.isNotEmpty() && businessDate != null -> Text(
                    stringResource(Res.string.feature_home_dashboard_meetings_summary, data.today.size, businessDate.shortDate()),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = c.onSurfaceVariant,
                )
            }
        }
        // 03: "Couldn't load this card · ⟳ Retry" under the title — its own full-width row, so a long
        // trailing link can never squeeze it (it wrapped one letter per line on a large-font phone).
        if (failed) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw), modifier = Modifier.padding(top = 0.5f.tw)) {
                Text(stringResource(Res.string.feature_home_dashboard_card_error), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.finance.moneyNegative)
                Text(META_DOT, style = KptTheme.typography.labelMedium, color = c.onSurfaceVariant)
                Row(
                    modifier = Modifier.heightIn(min = 8.tw).clip(KptTheme.shapes.extraSmall).clickable(role = Role.Button, onClick = onRetry).padding(horizontal = 1.tw),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.5f.tw),
                ) {
                    Icon(AppIcons.Refresh, contentDescription = null, tint = c.primary, modifier = Modifier.size(3.5f.tw))
                    Text(stringResource(Res.string.feature_home_dashboard_retry), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = c.primary)
                }
            }
        }
        if (failedOverCache) CardError(onRetry, error = content?.freshnessSignal?.lastError, cachedAt = content?.fetchedAt)
        card()
    }
}

/** 03: `bg-white rounded-2xl p-4 border shadow-sm space-y-3` around a section — or just the column. */
@Composable
private fun SectionCard(carded: Boolean, modifier: Modifier, content: @Composable () -> Unit) {
    if (carded) {
        Surface(modifier, padding = 4.tw, shape = KptTheme.shapes.large, bordered = true) {
            Column(verticalArrangement = Arrangement.spacedBy(3.tw)) { content() }
        }
    } else {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(3.tw)) { content() }
    }
}

/** `text-xs font-semibold text-primary min-h-[48px]` — the section's trailing link. */
@Composable
private fun LinkAction(text: String, modifier: Modifier = Modifier, chevron: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = modifier.heightIn(min = 12.tw).clip(KptTheme.shapes.extraSmall).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 1.tw),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.5f.tw),
    ) {
        Text(text, style = KptTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = KptTheme.colorScheme.primary)
        if (chevron) Icon(AppIcons.ChevronRight, contentDescription = null, tint = KptTheme.colorScheme.primary, modifier = Modifier.size(4.tw))
    }
}

@Composable
private fun sectionSummary(data: HomeCard?): String? = when (data) {
    is HomeCard.Collections -> listOfNotNull(
        pluralStringResource(Res.plurals.feature_home_dashboard_collections_summary, data.clientsDue, data.clientsDue, data.dueByCurrency.joinToString(", ") { money(it.amount, it.currencyCode, 0) }),
        data.collectedByCurrency.takeIf { it.isNotEmpty() }?.let { m -> stringResource(Res.string.feature_home_dashboard_summary_collected, m.joinToString(", ") { money(it.amount, it.currencyCode, 0) }) },
        data.overdueClients?.let { stringResource(Res.string.feature_home_dashboard_summary_overdue, it) },
    ).joinToString(META_SEPARATOR).takeIf { data.clientsDue > 0 || data.collectedByCurrency.isNotEmpty() }
    is HomeCard.Portfolio -> listOfNotNull(
        data.activeClients?.let { stringResource(Res.string.feature_home_dashboard_summary_active_clients, it) },
        data.activeLoans?.let { stringResource(Res.string.feature_home_dashboard_summary_active_loans, it) },
        data.loanTotals?.takeIf { it.size == 1 }?.first()?.let { stringResource(Res.string.feature_home_dashboard_summary_outstanding, money(it.outstanding, it.currencyCode, 0)) },
        data.loanTotals?.takeIf { it.size == 1 }?.first()?.par?.let { stringResource(Res.string.feature_home_dashboard_summary_par, percent(it)) },
    ).takeIf { it.isNotEmpty() }?.joinToString(META_SEPARATOR)
    else -> null
}

private fun money(amount: Double, code: String, decimals: Int = 2): String = CurrencyFormatter.format(amount, code, decimals)

private fun percent(fraction: Double): String = "${(fraction * PERCENT_TENTHS).toLong() / 10.0}%"

// ── 02 #collections_card ───────────────────────────────────────────────────────────────────────

@Composable
private fun CollectionsBody(data: HomeCard.Collections) = Column(verticalArrangement = Arrangement.spacedBy(3.tw)) {
    if (data.clientsDue == 0 && data.collectedByCurrency.isEmpty()) {
        Text(stringResource(Res.string.feature_home_dashboard_collections_empty), style = KptTheme.typography.bodyMedium, color = KptTheme.colorScheme.onSurfaceVariant)
        return@Column
    }
    // One hero per currency the day touches (Stitch draws one; a mixed-currency day must not sum across).
    val codes = (data.dueByCurrency.map { it.currencyCode } + data.collectedByCurrency.map { it.currencyCode }).distinct()
    codes.forEach { code -> CollectedHero(code, data) }
    val c = KptTheme.colorScheme
    val f = MaterialTheme.finance
    // grid grid-cols-2 gap-3 pt-1
    Grid(columns = 2, gap = 3.tw, modifier = Modifier.padding(top = 1.tw)) {
        add {
            StatTile(stringResource(Res.string.feature_home_dashboard_collections_due_clients), AppIcons.Groups, c.primaryContainer, c.primary) {
                val text = pluralStringResource(Res.plurals.feature_home_dashboard_collections_clients_value, data.clientsDue, data.clientsDue)
                val n = data.clientsDue.toString()
                val unit = c.onSurfaceVariant
                // "14 clients" — the figure text-xl bold, its unit text-xs normal muted
                Text(
                    buildAnnotatedString {
                        val at = text.indexOf(n)
                        if (at < 0) {
                            append(text)
                            return@buildAnnotatedString
                        }
                        withStyle(SpanStyle(fontSize = KptTheme.typography.labelMedium.fontSize, fontWeight = FontWeight.Normal, color = unit)) { append(text.substring(0, at)) }
                        append(n)
                        withStyle(SpanStyle(fontSize = KptTheme.typography.labelMedium.fontSize, fontWeight = FontWeight.Normal, color = unit)) { append(text.substring(at + n.length)) }
                    },
                    style = KptTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.onSurface,
                )
            }
        }
        add {
            StatTile(stringResource(Res.string.feature_home_dashboard_collections_due_amount), Icons.AutoMirrored.Outlined.ReceiptLong, c.primaryContainer, c.primary) {
                data.dueByCurrency.forEach { Figure(money(it.amount, it.currencyCode, 0), KptTheme.typography.titleLarge, c.onSurface) }
            }
        }
        add {
            StatTile(stringResource(Res.string.feature_home_dashboard_collections_collected), Icons.Outlined.CheckCircle, c.secondaryContainer, c.secondary) {
                val collected = data.collectedByCurrency.ifEmpty { data.dueByCurrency.map { it.copy(amount = 0.0) } }
                collected.forEach { Figure(money(it.amount, it.currencyCode, 0), KptTheme.typography.titleLarge, f.moneyPositive) }
            }
        }
        data.overdueClients?.let { overdue ->
            add {
                StatTile(stringResource(Res.string.feature_home_dashboard_collections_overdue), Icons.Outlined.Warning, f.moneyNegativeContainer, f.moneyNegative) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.tw)) {
                        Figure("$overdue", KptTheme.typography.titleLarge, f.moneyNegative)
                        Chip(
                            stringResource(Res.string.feature_home_dashboard_collections_overdue_chip),
                            container = f.moneyNegativeContainer,
                            content = f.moneyNegative,
                            style = KptTheme.typography.labelMedium,
                            shape = KptTheme.shapes.extraSmall,
                            horizontal = 1.5f.tw,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 02 lead KPI card: `rounded-2xl bg-gradient-to-br … p-5 shadow-elevated` with the blurred
 * `-right-6 -bottom-6 w-32 h-32 bg-white/10` disc. Goal = still due + already collected.
 */
@Composable
private fun CollectedHero(code: String, data: HomeCard.Collections) {
    val due = data.dueByCurrency.firstOrNull { it.currencyCode == code }?.amount ?: 0.0
    val collected = data.collectedByCurrency.firstOrNull { it.currencyCode == code }?.amount ?: 0.0
    val goal = due + collected
    val fraction = if (goal > 0) (collected / goal).toFloat().coerceIn(0f, 1f) else 0f
    val pct = percent(fraction.toDouble())
    val on = KptTheme.colorScheme.onPrimary
    val muted = AccentColors.heroText
    val shape = KptTheme.shapes.large
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(ELEVATED_SHADOW, shape)
            .clip(shape)
            .background(Brush.linearGradient(listOf(AccentColors.heroStart, AccentColors.heroMid, AccentColors.heroEnd)))
            .testTag(TestTags.Home.COLLECTIONS_HERO),
    ) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.tw, y = 6.tw)
                .size(32.tw)
                .blur(6.tw)
                .background(on.copy(alpha = HERO_DISC), CircleShape),
        )
        Column(Modifier.padding(5.tw), verticalArrangement = Arrangement.spacedBy(4.tw)) {
            Row(verticalAlignment = Alignment.Top) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                    Icon(AppIcons.Payments, contentDescription = null, tint = muted, modifier = Modifier.size(4.tw))
                    Text(
                        stringResource(Res.string.feature_home_dashboard_collections_collected_today).uppercase(),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = WIDER,
                        color = muted,
                    )
                }
                Chip(
                    stringResource(Res.string.feature_home_dashboard_collections_of_goal, pct),
                    container = on.copy(alpha = HERO_PILL),
                    content = on,
                    style = KptTheme.typography.labelSmall,
                    weight = FontWeight.Bold,
                )
            }
            Column {
                Text(money(collected, code), style = KptTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, letterSpacing = TIGHT, color = on)
                Row(Modifier.padding(top = 1.tw)) {
                    Text(stringResource(Res.string.feature_home_dashboard_collections_goal, money(goal, code)), modifier = Modifier.weight(1f), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = AccentColors.heroText.copy(alpha = HERO_SUB))
                    Text(stringResource(Res.string.feature_home_dashboard_collections_remains, money(due, code)), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = AccentColors.heroRemains)
                }
            }
            // h-2.5 bg-black/20 rounded-full p-0.5 · fill from-emerald-300 to-emerald-400
            Column(verticalArrangement = Arrangement.spacedBy(1.tw)) {
                Box(Modifier.fillMaxWidth().height(2.5f.tw).clip(CircleShape).background(Color.Black.copy(alpha = TRACK_ALPHA)).padding(0.5f.tw)) {
                    Box(
                        Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(AccentColors.heroFillStart, AccentColors.heroFillEnd))),
                    )
                }
                Row {
                    val label = KptTheme.typography.labelSmall
                    Text(money(0.0, code), style = label, color = AccentColors.heroFaint)
                    Text(stringResource(Res.string.feature_home_dashboard_collections_percent_collected, pct), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = label, color = AccentColors.heroFaint)
                    Text(money(goal, code), style = label, color = AccentColors.heroFaint)
                }
            }
        }
    }
}

/** `bg-surface-card rounded-xl p-3.5 shadow-card` — label + w-7 icon box over a text-xl figure. */
@Composable
private fun StatTile(label: String, icon: ImageVector, tint: Color, glyph: Color, value: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), padding = 3.5f.tw) {
        Column {
            Row(Modifier.padding(bottom = 2.tw), verticalAlignment = Alignment.CenterVertically) {
                Text(label, modifier = Modifier.weight(1f), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = KptTheme.colorScheme.onSurfaceVariant)
                IconBox(icon, tint, glyph, box = 7.tw, glyphSize = 4.tw, shape = KptTheme.shapes.small)
            }
            value()
        }
    }
}

// ── 02 #work_queue_card ────────────────────────────────────────────────────────────────────────

@Composable
private fun WorkQueueBody(data: HomeCard.WorkQueue, draftCount: Int, onNavigate: (HomeNavigation) -> Unit) {
    val c = KptTheme.colorScheme
    // grid grid-cols-2 gap-2.5 — each tile carries its own Stitch tint
    Grid(columns = 2, gap = 2.5f.tw) {
        data.checkerTasks?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_checker, n, Icons.AutoMirrored.Outlined.FactCheck, c.primaryContainer, c.primary, TestTags.Home.QUEUE_CHECKER) { onNavigate(HomeNavigation.CheckerInbox) } }
        }
        data.awaitingApproval?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_approval, n, Icons.Outlined.PendingActions, c.tertiaryContainer, c.tertiary, TestTags.Home.QUEUE_APPROVAL) { onNavigate(HomeNavigation.LoanQueue(DashboardRepository.STATUS_SUBMITTED)) } }
        }
        data.awaitingDisbursal?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_disbursal, n, Icons.Outlined.Verified, c.secondaryContainer, c.secondary, TestTags.Home.QUEUE_DISBURSAL) { onNavigate(HomeNavigation.LoanQueue(DashboardRepository.STATUS_APPROVED)) } }
        }
        data.pendingClients?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_pending_clients, n, Icons.Outlined.HowToReg, AccentColors.blueContainer, AccentColors.blue, TestTags.Home.QUEUE_PENDING_CLIENTS) { onNavigate(HomeNavigation.PendingClients) } }
        }
        data.pendingReschedules?.let { n ->
            add { QueueTile(Res.string.feature_home_dashboard_queue_reschedules, n, Icons.Outlined.EventRepeat, AccentColors.purpleContainer, AccentColors.purple, TestTags.Home.QUEUE_RESCHEDULES) { onNavigate(HomeNavigation.Reschedules) } }
        }
        // Stitch 02: the sixth tile is always there — "0" says there is nothing waiting to sync.
        add { DraftsTile(draftCount) { onNavigate(HomeNavigation.Drafts) } }
    }
}

/** `bg-surface-card rounded-xl p-3 shadow-card` — w-7 icon box + text-xs label, text-sm bold count top-right. */
@Composable
private fun QueueTile(label: StringResource, count: Int, icon: ImageVector, tint: Color, glyph: Color, tag: String, onClick: () -> Unit) {
    Surface(Modifier.fillMaxSize().testTag(tag), padding = 3.tw, onClick = onClick) {
        Row {
            Column(Modifier.weight(1f)) {
                IconBox(icon, tint, glyph, box = 7.tw, glyphSize = 4.tw, shape = KptTheme.shapes.small)
                Spacer(Modifier.height(3.tw))
                Text(stringResource(label), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = KptTheme.colorScheme.onSurface)
            }
            Text("$count", style = KptTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = KptTheme.colorScheme.onSurface)
        }
    }
}

/** The amber tile: `bg-amber-50/70 border-amber-200/60`, an amber-200 icon box, count on a tertiary badge. */
@Composable
private fun DraftsTile(count: Int, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    Surface(
        Modifier.fillMaxSize().testTag(TestTags.Home.QUEUE_DRAFTS),
        padding = 3.tw,
        // opaque blend, not alpha: a translucent Card shows its own shadow through as a dark frame
        color = lerp(c.surfaceContainerLowest, c.tertiaryContainer, DRAFTS_FILL),
        border = BorderStroke(HAIRLINE, lerp(c.tertiaryContainer, c.tertiary, DRAFTS_EDGE)),
        onClick = onClick,
    ) {
        Row {
            Column(Modifier.weight(1f)) {
                IconBox(Icons.Outlined.SyncProblem, lerp(c.tertiaryContainer, c.tertiary, DRAFTS_BOX), c.onTertiaryContainer, box = 7.tw, glyphSize = 4.tw, shape = KptTheme.shapes.small)
                Spacer(Modifier.height(3.tw))
                Text(stringResource(Res.string.feature_home_dashboard_queue_drafts), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = c.onTertiaryContainer)
            }
            Chip("$count", container = c.tertiary, content = c.onTertiary, style = KptTheme.typography.labelMedium, weight = FontWeight.Bold, shape = KptTheme.shapes.extraSmall, horizontal = 1.5f.tw)
        }
    }
}

// ── 02 #meetings_card ──────────────────────────────────────────────────────────────────────────

/** "10:00 AM" from the calendar's "HH:mm". */
private fun String.clock12(): String {
    val (h, m) = split(':').map { it.toIntOrNull() ?: 0 }
    val hour = (h + HOURS_HALF_DAY - 1) % HOURS_HALF_DAY + 1
    return "${hour.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')} ${if (h < HOURS_HALF_DAY) "AM" else "PM"}"
}

@Composable
private fun MeetingsBody(data: HomeCard.Meetings, onNavigate: (HomeNavigation) -> Unit) {
    val c = KptTheme.colorScheme
    if (data.today.isEmpty()) {
        Text(stringResource(Res.string.feature_home_dashboard_meetings_empty), style = KptTheme.typography.bodyMedium, color = c.onSurfaceVariant)
        return
    }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    val nowHm = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
    Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        data.today.forEach { meeting ->
            // On schedule once the meeting time has come; Upcoming before it; Today when no time is set.
            val started = meeting.time?.let { it <= nowHm }
            Surface(Modifier.fillMaxWidth().testTag(TestTags.Home.MEETING_ROW), padding = 3.5f.tw, onClick = { onNavigate(HomeNavigation.Center(meeting.centerId)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.tw)) {
                        IconBox(Icons.Outlined.MeetingRoom, c.primaryContainer, c.primary, box = 10.tw, glyphSize = 6.tw, shape = KptTheme.shapes.small, modifier = Modifier.padding(top = 0.5f.tw))
                        Column {
                            Text(meeting.name, style = KptTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(Res.string.feature_home_dashboard_meeting_center_groups, meeting.groups), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant)
                            meeting.time?.let { t ->
                                val tone = if (started == true) c.primary else c.onSurfaceVariant
                                Row(Modifier.padding(top = 1.tw), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.tw)) {
                                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = tone, modifier = Modifier.size(3.5f.tw))
                                    Text(t.clock12(), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = tone)
                                }
                            }
                        }
                    }
                    // px-2.5 py-1 text-[11px] font-semibold rounded-full
                    when (started) {
                        true -> Chip(stringResource(Res.string.feature_home_dashboard_meeting_on_schedule), c.secondaryContainer, c.secondary, KptTheme.typography.labelSmall, horizontal = 2.5f.tw, vertical = 1.tw, border = c.secondary.copy(alpha = EDGE_ALPHA))
                        false -> Chip(stringResource(Res.string.feature_home_dashboard_meeting_upcoming), c.surfaceContainer, c.onSurfaceVariant, KptTheme.typography.labelSmall, horizontal = 2.5f.tw, vertical = 1.tw)
                        null -> Chip(stringResource(Res.string.feature_home_dashboard_meeting_today), c.primaryContainer, c.primary, KptTheme.typography.labelSmall, horizontal = 2.5f.tw, vertical = 1.tw)
                    }
                }
            }
        }
    }
}

// ── 02 #portfolio_card ─────────────────────────────────────────────────────────────────────────

@Composable
private fun PortfolioBody(data: HomeCard.Portfolio) {
    val c = KptTheme.colorScheme
    val f = MaterialTheme.finance
    val totals = data.loanTotals?.takeIf { it.isNotEmpty() }
    val plain = c.surfaceContainer.copy(alpha = TILE_FILL) to c.surfaceContainerHigh
    // grid grid-cols-2 gap-2.5 — the outstanding tile spans both columns (03 #portfolio_card)
    Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        Pair(
            data.activeClients?.let { n -> @Composable { PortfolioTile(Res.string.feature_home_dashboard_tile_active_clients, plain) { Figure("$n") } } },
            data.activeLoans?.let { n -> @Composable { PortfolioTile(Res.string.feature_home_dashboard_tile_active_loans, plain) { Figure("$n") } } },
        ).Row2()
        totals?.let { t ->
            // col-span-2 p-3 bg-primary-container/40 border-indigo-200 — label + bank glyph, text-xl extrabold
            Box(
                Modifier.fillMaxWidth().clip(KptTheme.shapes.medium).background(c.primaryContainer.copy(alpha = OUTSTANDING_FILL))
                    .border(HAIRLINE, c.primary.copy(alpha = EDGE_ALPHA), KptTheme.shapes.medium).padding(3.tw),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(Res.string.feature_home_dashboard_portfolio_total_outstanding), Modifier.weight(1f), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = c.primary)
                        Icon(AppIcons.AccountBalance, contentDescription = null, tint = c.primary, modifier = Modifier.size(4.5f.tw))
                    }
                    t.forEach { Figure(money(it.outstanding, it.currencyCode), KptTheme.typography.titleLarge, c.onPrimaryContainer, FontWeight.ExtraBold, Modifier.padding(top = 0.5f.tw)) }
                }
            }
        }
        Pair(
            totals?.let { t ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_overdue, f.moneyNegativeContainer.copy(alpha = TINT_FILL) to f.moneyNegative.copy(alpha = SOFT_EDGE_ALPHA), labelColor = f.moneyNegative) {
                        t.forEach { Figure(money(it.overdue, it.currencyCode, 0), color = f.moneyNegative) }
                    }
                }
            },
            data.inArrears?.let { n -> @Composable { PortfolioTile(Res.string.feature_home_dashboard_tile_in_arrears, plain) { Figure("$n") } } },
        ).Row2()
        Pair(
            totals?.let { t ->
                @Composable {
                    // "PAR · tgt <5%" — amber tile above the benchmark, the healthy green figure within it
                    PortfolioTile(
                        Res.string.feature_home_dashboard_tile_par,
                        c.tertiaryContainer.copy(alpha = TINT_FILL) to c.tertiary.copy(alpha = EDGE_ALPHA),
                        labelColor = c.tertiary,
                        note = stringResource(Res.string.feature_home_dashboard_tile_par_target, percent(PAR_TARGET)),
                    ) {
                        t.forEach { x ->
                            val par = x.par ?: 0.0
                            Figure(if (t.size > 1) "${percent(par)} ${x.currencyCode}" else percent(par), color = if (par > PAR_TARGET) c.tertiary else f.moneyPositive)
                        }
                    }
                }
            },
            data.savings?.takeIf { it.isNotEmpty() }?.let { sv -> @Composable { PortfolioTile(Res.string.feature_home_dashboard_tile_savings, plain) { sv.forEach { Figure(money(it.amount, it.currencyCode, 0), color = f.moneyPositive) } } } },
        ).Row2()
        Pair(
            data.groups?.let { n -> @Composable { PortfolioTile(Res.string.feature_home_dashboard_tile_groups, plain) { Figure("$n") } } },
            data.centers?.let { n -> @Composable { PortfolioTile(Res.string.feature_home_dashboard_tile_centers, plain) { Figure("$n") } } },
        ).Row2()
    }
}

/** Two equal tiles in a row (gap-2.5); a missing one leaves its half empty, none renders nothing. */
@Composable
private fun Pair<(@Composable () -> Unit)?, (@Composable () -> Unit)?>.Row2() {
    if (first == null && second == null) return
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        listOf(first, second).forEach { tile -> Box(Modifier.weight(1f).fillMaxHeight()) { tile?.invoke() } }
    }
}

/** 03: `p-2.5 rounded-xl border` on a soft fill — text-[11px] label (+ optional note) over a text-base bold figure. */
@Composable
private fun PortfolioTile(label: StringResource, colors: Pair<Color, Color>, labelColor: Color = KptTheme.colorScheme.onSurfaceVariant, note: String? = null, value: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().clip(KptTheme.shapes.medium).background(colors.first)
            .border(HAIRLINE, colors.second, KptTheme.shapes.medium).padding(2.5f.tw),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(label), Modifier.weight(1f), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = labelColor)
                note?.let { Text(it, style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = KptTheme.colorScheme.onSurfaceVariant) }
            }
            value()
        }
    }
}

// ── #quick_actions (02 content · 04 error · 05 offline) ────────────────────────────────────────

private val QuickAction.label: StringResource
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Res.string.feature_home_dashboard_qa_new_client
        QuickAction.NEW_GROUP -> Res.string.feature_home_dashboard_qa_new_group
        QuickAction.NEW_CENTER -> Res.string.feature_home_dashboard_qa_new_center
        QuickAction.COLLECTION_SHEET -> Res.string.feature_home_dashboard_qa_collection_sheet
        QuickAction.RUN_REPORTS -> Res.string.feature_home_dashboard_qa_run_reports
        QuickAction.SEARCH -> Res.string.feature_home_dashboard_qa_search
        QuickAction.PATH_TRACKER -> Res.string.feature_home_dashboard_qa_path_tracker
    }

private val QuickAction.icon: ImageVector
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Icons.Outlined.PersonAdd
        QuickAction.NEW_GROUP -> AppIcons.Groups
        QuickAction.NEW_CENTER -> Icons.Outlined.Domain
        QuickAction.COLLECTION_SHEET -> Icons.AutoMirrored.Outlined.FactCheck
        QuickAction.RUN_REPORTS -> Icons.Outlined.BarChart
        QuickAction.SEARCH -> AppIcons.Search
        QuickAction.PATH_TRACKER -> AppIcons.PersonPinCircle
    }

private val QuickAction.tag: String
    get() = when (this) {
        QuickAction.NEW_CLIENT -> TestTags.Home.QA_CLIENT
        QuickAction.NEW_GROUP -> TestTags.Home.QA_GROUP
        QuickAction.NEW_CENTER -> TestTags.Home.QA_CENTER
        QuickAction.COLLECTION_SHEET -> TestTags.Home.QA_COLLECTION
        QuickAction.RUN_REPORTS -> TestTags.Home.QA_REPORTS
        QuickAction.SEARCH -> TestTags.Home.QA_SEARCH
        QuickAction.PATH_TRACKER -> TestTags.Home.QA_PATH_TRACKER
    }

/** 05 gives each action its own tint (brand · indigo · blue · amber · purple · slate). */
@Composable
private fun QuickAction.tint(): Pair<Color, Color> {
    val c = KptTheme.colorScheme
    return when (this) {
        QuickAction.NEW_CLIENT -> c.primaryContainer to c.primary
        QuickAction.NEW_GROUP -> c.primaryContainer to c.primary
        QuickAction.NEW_CENTER -> AccentColors.blueContainer to AccentColors.blue
        QuickAction.COLLECTION_SHEET -> c.tertiaryContainer to c.tertiary
        QuickAction.RUN_REPORTS -> AccentColors.purpleContainer to AccentColors.purple
        QuickAction.SEARCH -> c.surfaceContainer to c.onSurfaceVariant
        QuickAction.PATH_TRACKER -> c.secondaryContainer to c.secondary
    }
}

private enum class QuickStyle { Content, Error, Offline }

/** 03: client indigo · group emerald · centre amber · sheet indigo-50 · reports + search slate. */
@Composable
private fun QuickAction.cardTint(): Pair<Color, Color> {
    val c = KptTheme.colorScheme
    return when (this) {
        QuickAction.NEW_CLIENT -> c.primaryContainer to c.primary
        QuickAction.NEW_GROUP -> c.secondaryContainer to c.secondary
        QuickAction.NEW_CENTER -> c.tertiaryContainer to c.tertiary
        QuickAction.COLLECTION_SHEET -> c.primaryContainer to c.primary
        QuickAction.RUN_REPORTS -> c.surfaceContainer to c.onSurfaceVariant
        QuickAction.SEARCH -> c.surfaceContainer to c.onSurfaceVariant
        QuickAction.PATH_TRACKER -> c.secondaryContainer to c.secondary
    }
}

private val QuickAction.cardIcon: ImageVector
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Icons.Outlined.PersonAdd
        QuickAction.NEW_GROUP -> Icons.Outlined.GroupAdd
        QuickAction.NEW_CENTER -> Icons.Outlined.DomainAdd
        QuickAction.COLLECTION_SHEET -> Icons.AutoMirrored.Outlined.ReceiptLong
        QuickAction.RUN_REPORTS -> Icons.Outlined.Analytics
        QuickAction.SEARCH -> AppIcons.Search
        QuickAction.PATH_TRACKER -> AppIcons.PersonPinCircle
    }

@Composable
private fun QuickActions(actions: List<QuickAction>, onNavigate: (HomeNavigation) -> Unit, style: QuickStyle) {
    if (actions.isEmpty()) return
    val c = KptTheme.colorScheme
    if (style == QuickStyle.Content) {
        SectionCard(true, Modifier.fillMaxWidth().testTag(TestTags.Home.QUICK_ACTIONS)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.feature_home_dashboard_quick_actions_title), Modifier.weight(1f), style = KptTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.onSurface)
                Chip(stringResource(Res.string.feature_home_dashboard_quick_actions_available, actions.size), c.secondaryContainer, c.secondary, KptTheme.typography.labelSmall, weight = FontWeight.Bold)
            }
            // grid-cols-3 gap-2 · p-3 rounded-xl bg-surface-container border · w-9 rounded-full · text-xs semibold
            Grid(columns = 3, gap = 2.tw) {
                actions.forEach { action ->
                    add {
                        val (tint, glyph) = action.cardTint()
                        Box(
                            Modifier.fillMaxSize().heightIn(min = 12.tw).clip(KptTheme.shapes.medium).background(c.surfaceContainerLow)
                                .border(HAIRLINE, c.surfaceContainerHigh, KptTheme.shapes.medium)
                                .clickable(role = Role.Button) { onNavigate(HomeNavigation.Quick(action)) }
                                .padding(3.tw).testTag(action.tag),
                        ) {
                            QuickTileBody(action.cardIcon, stringResource(action.label), tint, glyph, box = 9.tw, glyph = 5.tw, shape = CircleShape, gap = 1.5f.tw, weight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        return
    }
    Column(
        Modifier.testTag(TestTags.Home.QUICK_ACTIONS).padding(top = if (style == QuickStyle.Offline) 2.tw else 0.tw),
        verticalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = stringResource(Res.string.feature_home_dashboard_quick_actions_title)
            when (style) {
                // 04: text-sm bold · 02/05: text-sm bold uppercase tracking-wider
                QuickStyle.Error -> Text(title, Modifier.weight(1f), style = KptTheme.typography.titleSmall, fontWeight = FontWeight.Bold, letterSpacing = TIGHT, color = c.onSurface)
                else -> Text(title.uppercase(), Modifier.weight(1f), style = KptTheme.typography.titleSmall, fontWeight = FontWeight.Bold, letterSpacing = WIDER, color = c.onSurface)
            }
            // 04 / 05: the actions work without a network — every create keeps a draft and syncs later.
            run {
                Chip(
                    stringResource(Res.string.feature_home_dashboard_offline_enabled),
                    container = c.secondaryContainer,
                    content = c.secondary,
                    style = KptTheme.typography.labelSmall,
                    border = c.secondary.copy(alpha = EDGE_ALPHA),
                    leading = if (style == QuickStyle.Error) AppIcons.CloudOff else null,
                )
            }
        }
        Grid(columns = 3, gap = 2.5f.tw) {
            actions.forEach { action ->
                add {
                    val label = stringResource(action.label)
                    val onClick = { onNavigate(HomeNavigation.Quick(action)) }
                    when (style) {
                        // 04: bordered min-h-[76px] tile · w-9 rounded-lg primary-container · text-xs medium
                        QuickStyle.Error -> Surface(Modifier.fillMaxSize().heightIn(min = 19.tw).testTag(action.tag), padding = 3.tw, bordered = true, onClick = onClick) {
                            QuickTileBody(action.icon, label, c.primaryContainer, c.primary, box = 9.tw, glyph = 5.tw, shape = KptTheme.shapes.small, gap = 1.5f.tw, weight = FontWeight.Medium)
                        }
                        // 05: bordered tile · w-10 rounded-xl, tinted per action · text-xs semibold
                        else -> Surface(Modifier.fillMaxSize().testTag(action.tag), padding = 3.tw, bordered = true, onClick = onClick) {
                            val (tint, glyph) = action.tint()
                            QuickTileBody(action.icon, label, tint, glyph, box = 10.tw, glyph = 5.tw, shape = KptTheme.shapes.medium, gap = 2.tw, weight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickTileBody(icon: ImageVector, label: String, tint: Color, glyphColor: Color, box: Dp, glyph: Dp, shape: Shape, gap: Dp, weight: FontWeight) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        IconBox(icon, tint, glyphColor, box = box, glyphSize = glyph, shape = shape)
        Spacer(Modifier.height(gap))
        Text(label, style = KptTheme.typography.labelMedium, fontWeight = weight, textAlign = TextAlign.Center, maxLines = 2, color = KptTheme.colorScheme.onSurface)
    }
}

// ── #recent_card (02 content · 04 error · 05 offline) ──────────────────────────────────────────

private val RecentKind.label: StringResource
    get() = when (this) {
        RecentKind.CLIENT -> Res.string.feature_home_dashboard_recent_kind_client
        RecentKind.GROUP -> Res.string.feature_home_dashboard_recent_kind_group
        RecentKind.CENTER -> Res.string.feature_home_dashboard_recent_kind_center
        RecentKind.LOAN -> Res.string.feature_home_dashboard_recent_kind_loan
    }

private val RecentKind.icon: ImageVector
    get() = when (this) {
        RecentKind.CLIENT -> AppIcons.Person
        RecentKind.GROUP -> AppIcons.Groups
        RecentKind.CENTER -> Icons.Outlined.Domain
        RecentKind.LOAN -> Icons.Outlined.RequestQuote
    }

/** 02: client indigo · group emerald · loan blue · centre amber. */
@Composable
private fun RecentKind.tint(): Pair<Color, Color> {
    val c = KptTheme.colorScheme
    return when (this) {
        RecentKind.CLIENT -> c.primaryContainer to c.primary
        RecentKind.GROUP -> c.secondaryContainer to c.secondary
        RecentKind.LOAN -> c.primaryContainer to c.onPrimaryContainer
        RecentKind.CENTER -> c.tertiaryContainer to c.tertiary
    }
}

/** "12m ago" / "2h ago" / "Yesterday" — how long since the officer last opened the record. */
@Composable
private fun ago(openedAtMs: Long): String {
    val minutes = ((Clock.System.now().toEpochMilliseconds() - openedAtMs) / MS_PER_MINUTE).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(Res.string.feature_home_dashboard_time_just_now)
        minutes < MINUTES_PER_HOUR -> stringResource(Res.string.feature_home_dashboard_time_minutes_ago, minutes.toInt())
        minutes < MINUTES_PER_DAY -> stringResource(Res.string.feature_home_dashboard_time_hours_ago, (minutes / MINUTES_PER_HOUR).toInt())
        minutes < 2 * MINUTES_PER_DAY -> stringResource(Res.string.feature_home_dashboard_time_yesterday)
        else -> stringResource(Res.string.feature_home_dashboard_time_days_ago, (minutes / MINUTES_PER_DAY).toInt())
    }
}

private fun String.initials(): String =
    split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1) }.uppercase()

@Composable
private fun RecentRecord.subtitle(): String =
    listOfNotNull(detail ?: stringResource(kind.label), accountNo?.let { "#$it" }).joinToString(META_SEPARATOR)

private enum class RecentStyle { Content, Error, Offline }

/** Hidden until something has been opened. */
@Composable
private fun RecentCard(recent: List<RecentRecord>, onNavigate: (HomeNavigation) -> Unit, style: RecentStyle) {
    if (recent.isEmpty()) return
    val c = KptTheme.colorScheme
    if (style == RecentStyle.Content) {
        SectionCard(true, Modifier.fillMaxWidth().testTag(TestTags.Home.RECENT)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.feature_home_dashboard_recent_title), Modifier.weight(1f), style = KptTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.onSurface)
                Chip(stringResource(Res.string.feature_home_dashboard_recent_cached, recent.size), c.surfaceContainer, c.onSurfaceVariant, KptTheme.typography.labelSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.tw)) { recent.forEach { r -> RecentRowContent(r) { onNavigate(HomeNavigation.Recent(r.kind, r.id)) } } }
        }
        return
    }
    Column(
        Modifier.testTag(TestTags.Home.RECENT).padding(top = if (style == RecentStyle.Offline) 2.tw else 0.tw),
        verticalArrangement = Arrangement.spacedBy(if (style == RecentStyle.Error) 2.5f.tw else 3.tw),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val title = stringResource(Res.string.feature_home_dashboard_recent_title)
            when (style) {
                RecentStyle.Content -> {
                    Text(title, Modifier.weight(1f), style = KptTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = c.onSurface)
                    Chip(stringResource(Res.string.feature_home_dashboard_recent_cached, recent.size), c.surfaceContainer, c.onSurfaceVariant, KptTheme.typography.labelSmall)
                }
                else -> {
                    Text(
                        if (style == RecentStyle.Offline) title.uppercase() else title,
                        Modifier.weight(1f),
                        style = KptTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = if (style == RecentStyle.Offline) WIDER else TIGHT,
                        color = c.onSurface,
                    )
                    Text(stringResource(Res.string.feature_home_dashboard_recent_cached, recent.size), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = c.onSurfaceVariant)
                }
            }
        }
        // 02 rounded-2xl shadow-card · 04 rounded-xl bordered · 05 rounded-2xl bordered — rows split by dividers
        Surface(
            Modifier.fillMaxWidth(),
            shape = if (style == RecentStyle.Error) KptTheme.shapes.medium else KptTheme.shapes.large,
            bordered = style != RecentStyle.Content,
        ) {
            Column {
                recent.forEachIndexed { i, record ->
                    if (i > 0) HorizontalDivider(thickness = HAIRLINE, color = c.surfaceContainerHigh)
                    val open = { onNavigate(HomeNavigation.Recent(record.kind, record.id)) }
                    if (style == RecentStyle.Error) RecentRowError(record, open) else RecentRowOffline(record, open)
                }
            }
        }
    }
}

/** 03: `p-3 rounded-xl bg-surface-container border` · w-8 initials circle (kind/10) · text-sm bold · time over a chevron. */
@Composable
private fun RecentRowContent(record: RecentRecord, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    val (fill, ink) = when (record.kind) {
        RecentKind.CLIENT -> c.primary.copy(alpha = INITIALS_FILL) to c.primary
        RecentKind.GROUP -> c.secondary.copy(alpha = INITIALS_FILL) to c.secondary
        RecentKind.LOAN -> c.tertiary.copy(alpha = INITIALS_FILL) to c.tertiary
        RecentKind.CENTER -> c.surfaceContainerHigh to c.onSurfaceVariant
    }
    Row(
        Modifier.fillMaxWidth().clip(KptTheme.shapes.medium).background(c.surfaceContainerLow)
            .border(HAIRLINE, c.surfaceContainerHigh, KptTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick).padding(3.tw).testTag(TestTags.Home.RECENT_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Box(Modifier.size(8.tw).background(fill, CircleShape), contentAlignment = Alignment.Center) {
            Text(record.title.initials(), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ink)
        }
        Column(Modifier.weight(1f)) {
            Text(record.title, style = KptTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.onSurface)
            Text(record.subtitle(), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(ago(record.openedAtMs), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant)
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = c.onSurfaceVariant, modifier = Modifier.size(4.5f.tw))
        }
    }
}

/** 04: px-3.5 py-3 min-h-56 · w-8 slate circle · text-xs semibold name + kind chip · text-[11px] meta + chevron. */
@Composable
private fun RecentRowError(record: RecentRecord, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().heightIn(min = 14.tw).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 3.5f.tw, vertical = 3.tw).testTag(TestTags.Home.RECENT_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        IconBox(record.kind.icon, c.surfaceContainer, c.onSurfaceVariant, box = 8.tw, glyphSize = 4.5f.tw, shape = CircleShape)
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                Text(record.title, Modifier.weight(1f, fill = false), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Chip(stringResource(record.kind.label), c.surfaceContainer, c.onSurfaceVariant, KptTheme.typography.labelSmall, weight = FontWeight.Medium, shape = KptTheme.shapes.extraSmall, horizontal = 1.5f.tw, vertical = 0.tw)
            }
            Text(record.subtitle(), Modifier.padding(top = 0.5f.tw), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw)) {
            Text(ago(record.openedAtMs), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant)
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = c.onSurfaceVariant, modifier = Modifier.size(4.5f.tw))
        }
    }
}

/** 05: p-3.5 · w-10 tinted circle with initials · text-sm bold name + kind chip · text-xs meta + chevron. */
@Composable
private fun RecentRowOffline(record: RecentRecord, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    val (tint, glyph) = record.kind.tint()
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(3.5f.tw).testTag(TestTags.Home.RECENT_ROW),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Box(Modifier.size(10.tw).background(tint, CircleShape), contentAlignment = Alignment.Center) {
            if (record.kind == RecentKind.LOAN) {
                Icon(AppIcons.Payments, contentDescription = null, tint = glyph, modifier = Modifier.size(4.5f.tw))
            } else {
                Text(record.title.initials(), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = glyph)
            }
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                Text(record.title, Modifier.weight(1f, fill = false), style = KptTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Chip(stringResource(record.kind.label), c.surfaceContainer, c.onSurfaceVariant, KptTheme.typography.labelSmall, shape = KptTheme.shapes.extraSmall, horizontal = 1.5f.tw, vertical = 0.tw)
            }
            Text(record.subtitle(), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Normal, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(1.5f.tw)) {
            Text(ago(record.openedAtMs), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Medium, color = c.outline)
            Icon(AppIcons.ChevronRight, contentDescription = null, tint = c.outline, modifier = Modifier.size(4.tw))
        }
    }
}

// ── 03 card error · 04 screen error · 05 offline error + banner ───────────────────────────────

/**
 * What went wrong, in two parts — 03's "**Synchronization Timeout:** Network synchronization timed out
 * for this section": a bold kind (with the HTTP status when there is one), then the server's own
 * sentence. Fineract's `defaultUserMessage` comes through `ForkErrorMapper`; without a Fineract
 * body the categorised copy shows — never the raw transport message (that is the whole request URL
 * plus the JSON body).
 */
@Composable
private fun errorKind(error: Throwable?, offline: Boolean): String {
    if (offline || error == null) {
        return stringResource(if (offline) Res.string.feature_home_dashboard_error_kind_offline else Res.string.feature_home_dashboard_error_kind_failed)
    }
    return when (val cat = categorize(error)) {
        ErrorCategory.Network -> stringResource(Res.string.feature_home_dashboard_error_kind_offline)
        ErrorCategory.Timeout.Connect, ErrorCategory.Timeout.Read -> stringResource(Res.string.feature_home_dashboard_error_kind_timeout)
        ErrorCategory.Auth -> "${stringResource(Res.string.feature_home_dashboard_error_kind_signed_out)} (${HTTP_UNAUTHORIZED})"
        ErrorCategory.RateLimit, ErrorCategory.QuotaExceeded -> stringResource(Res.string.feature_home_dashboard_error_kind_busy)
        is ErrorCategory.ClientError -> {
            val kind = when (cat.httpCode) {
                HTTP_FORBIDDEN -> Res.string.feature_home_dashboard_error_kind_forbidden
                HTTP_NOT_FOUND -> Res.string.feature_home_dashboard_error_kind_not_found
                else -> Res.string.feature_home_dashboard_error_kind_rejected
            }
            "${stringResource(kind)} (${cat.httpCode})"
        }
        is ErrorCategory.Server -> stringResource(Res.string.feature_home_dashboard_error_kind_server) + (cat.httpCode?.let { " ($it)" } ?: "")
        ErrorCategory.Generic -> stringResource(Res.string.feature_home_dashboard_error_kind_failed)
    }
}

/** 03 `#card_error_retry`: rounded-xl red-200 border, red-50/80 fill, p-3.5; error glyph, cached time, the server's reason, red Retry. */
@Composable
private fun CardError(onRetry: () -> Unit, error: Throwable? = null, offline: Boolean = false, cachedAt: Instant? = null) {
    val f = MaterialTheme.finance
    val c = KptTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .clip(KptTheme.shapes.medium)
            .background(f.moneyNegativeContainer.copy(alpha = ERROR_FILL))
            .border(HAIRLINE, f.moneyNegative.copy(alpha = EDGE_ALPHA), KptTheme.shapes.medium)
            .padding(3.5f.tw)
            .testTag(TestTags.Home.CARD_ERROR),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = f.moneyNegative, modifier = Modifier.padding(top = 0.5f.tw).size(5.tw))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.tw)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.feature_home_dashboard_card_error), modifier = Modifier.weight(1f), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = f.moneyNegative)
                    cachedAt?.let {
                        val t = it.toLocalDateTime(TimeZone.currentSystemDefault()).time
                        val hm = "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
                        Text(stringResource(Res.string.feature_home_dashboard_card_cached, hm.clock12()), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = c.onSurfaceVariant)
                    }
                }
                // text-[12px] slate-700 leading-relaxed · <strong class="font-medium text-slate-900">Kind:</strong> reason
                val kind = errorKind(error, offline)
                val reason = error?.let { rememberAppErrorMessageFor()(it) } ?: stringResource(Res.string.feature_home_dashboard_card_error_body)
                val strong = c.onSurface
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = strong)) { append("$kind: ") }
                        append(reason)
                    },
                    modifier = Modifier.testTag(TestTags.Home.CARD_ERROR_REASON),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = c.onSurfaceVariant,
                    lineHeight = KptTheme.typography.bodyMedium.lineHeight,
                )
                // pt-1.5 · px-3 py-1.5 rounded-lg bg-moneyNegative text-xs semibold min-h-[36px]
                Row(
                    Modifier
                        .padding(top = 1.5f.tw)
                        .heightIn(min = 9.tw)
                        .shadow(CARD_SHADOW, KptTheme.shapes.small)
                        .clip(KptTheme.shapes.small)
                        .background(f.moneyNegative)
                        .clickable(role = Role.Button, onClick = onRetry)
                        .padding(horizontal = 3.tw, vertical = 1.5f.tw),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.5f.tw),
                ) {
                    Icon(Icons.Outlined.SyncProblem, contentDescription = null, tint = c.surfaceContainerLowest, modifier = Modifier.size(3.75f.tw))
                    Text(stringResource(Res.string.feature_home_dashboard_retry), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = c.surfaceContainerLowest)
                }
            }
        }
    }
}

/** 04 `#screen_error_retry`: white rounded-xl card, h-1 red top bar, w-12 sync-problem badge, full-width Retry. */
@Composable
private fun ScreenError(onRetryAll: () -> Unit) {
    val f = MaterialTheme.finance
    val c = KptTheme.colorScheme
    val edge = f.moneyNegative.copy(alpha = EDGE_ALPHA)
    Surface(Modifier.fillMaxWidth().testTag(TestTags.Home.SCREEN_ERROR), shape = KptTheme.shapes.medium, border = BorderStroke(HAIRLINE, edge), elevation = CARD_SHADOW) {
        Box {
            Column(Modifier.fillMaxWidth().padding(5.tw), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(12.tw).background(f.moneyNegativeContainer, CircleShape).border(HAIRLINE, edge, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.SyncProblem, contentDescription = null, tint = f.moneyNegative, modifier = Modifier.size(6.5f.tw)) }
                Spacer(Modifier.height(3.tw))
                Text(stringResource(Res.string.feature_home_dashboard_error_title), style = KptTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, letterSpacing = TIGHT, color = c.onSurface)
                Text(
                    stringResource(Res.string.feature_home_dashboard_error_body),
                    modifier = Modifier.padding(top = 1.5f.tw),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = c.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(stringResource(Res.string.feature_home_dashboard_retry), AppIcons.Sync, KptTheme.shapes.small, Modifier.padding(top = 4.tw), onRetryAll)
            }
            Box(Modifier.fillMaxWidth().height(1.tw).background(f.moneyNegative))
        }
    }
}

/** 05: white rounded-2xl p-6 card, w-16 rounded-2xl wifi-off badge, text-lg title, rounded-xl "Retry sync". */
@Composable
private fun OfflineError(onRetryAll: () -> Unit) {
    val f = MaterialTheme.finance
    val c = KptTheme.colorScheme
    val edge = f.moneyNegative.copy(alpha = SOFT_EDGE_ALPHA)
    Surface(Modifier.fillMaxWidth().testTag(TestTags.Home.SCREEN_ERROR), shape = KptTheme.shapes.large, border = BorderStroke(HAIRLINE, edge), elevation = ELEVATED_SHADOW) {
        Column(Modifier.fillMaxWidth().padding(6.tw), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(16.tw).shadow(CARD_SHADOW, KptTheme.shapes.large).background(f.moneyNegativeContainer, KptTheme.shapes.large).border(HAIRLINE, edge, KptTheme.shapes.large),
                contentAlignment = Alignment.Center,
            ) { Icon(AppIcons.WifiOff, contentDescription = null, tint = f.moneyNegative, modifier = Modifier.size(7.5f.tw)) }
            Spacer(Modifier.height(4.tw))
            Text(stringResource(Res.string.feature_home_dashboard_error_title), style = KptTheme.typography.titleMedium, fontWeight = FontWeight.Bold, letterSpacing = TIGHT, color = c.onSurface)
            Text(
                stringResource(Res.string.feature_home_dashboard_offline_body),
                modifier = Modifier.padding(top = 1.5f.tw),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(stringResource(Res.string.feature_home_dashboard_retry_sync), AppIcons.Refresh, KptTheme.shapes.medium, Modifier.padding(top = 5.tw), onRetryAll)
        }
    }
}

/** 05 resilience banner: rounded-xl p-3.5 emerald-50/80, emerald-200 border, cloud-sync glyph. */
@Composable
private fun OfflineBanner() {
    val c = KptTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(KptTheme.shapes.medium)
            .background(c.secondaryContainer.copy(alpha = ERROR_FILL))
            .border(HAIRLINE, c.secondary.copy(alpha = EDGE_ALPHA), KptTheme.shapes.medium)
            .padding(3.5f.tw)
            .testTag(TestTags.Home.OFFLINE_BANNER),
        horizontalArrangement = Arrangement.spacedBy(3.tw),
    ) {
        Icon(Icons.Outlined.CloudSync, contentDescription = null, tint = c.secondary, modifier = Modifier.padding(top = 0.5f.tw).size(5.tw))
        Column {
            Text(stringResource(Res.string.feature_home_dashboard_offline_banner_title), style = KptTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = c.onSecondaryContainer)
            Text(stringResource(Res.string.feature_home_dashboard_offline_banner_body), modifier = Modifier.padding(top = 0.5f.tw), style = KptTheme.typography.labelSmall, fontWeight = FontWeight.Normal, color = c.secondary)
        }
    }
}

/** Full-width min-h-[48px] primary button, text-sm semibold with a leading glyph. */
@Composable
private fun PrimaryButton(text: String, icon: ImageVector, shape: Shape, modifier: Modifier, onClick: () -> Unit) {
    val c = KptTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 12.tw)
            .shadow(CARD_SHADOW, shape)
            .clip(shape)
            .background(c.primary)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.tw, vertical = 2.5f.tw),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.tw, Alignment.CenterHorizontally),
    ) {
        Icon(icon, contentDescription = null, tint = c.onPrimary, modifier = Modifier.size(4.75f.tw))
        Text(text, style = KptTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = c.onPrimary)
    }
}

// ── 01 loading (#card_skeletons) ───────────────────────────────────────────────────────────────

@Composable
private fun DashboardSkeleton() {
    val c = KptTheme.colorScheme
    Column(Modifier.testTag(TestTags.Home.SKELETON), verticalArrangement = Arrangement.spacedBy(4.tw)) {
        // rounded-2xl bg-primary p-4 — shimmer-indigo bars, progress track, two sub-metrics over a divider
        Column(Modifier.fillMaxWidth().clip(KptTheme.shapes.large).background(c.primary).padding(4.tw)) {
            Row(Modifier.fillMaxWidth().padding(bottom = 3.tw), verticalAlignment = Alignment.CenterVertically) {
                OnPrimaryBar(28.tw, 4.tw)
                Spacer(Modifier.weight(1f))
                OnPrimaryBar(20.tw, 5.tw)
            }
            OnPrimaryBar(48.tw, 9.tw, KptTheme.shapes.small)
            Spacer(Modifier.height(2.tw))
            OnPrimaryBar(36.tw, 3.tw, KptTheme.shapes.extraSmall)
            Spacer(Modifier.height(4.tw))
            Box(Modifier.fillMaxWidth().height(2.tw).clip(CircleShape).background(Color.Black.copy(alpha = SKELETON_TRACK))) {
                OnPrimaryBar(null, 2.tw, fraction = 2f / 3f)
            }
            Spacer(Modifier.height(4.tw))
            HorizontalDivider(thickness = HAIRLINE, color = c.onPrimary.copy(alpha = SKELETON_RULE))
            Row(Modifier.padding(top = 3.tw), horizontalArrangement = Arrangement.spacedBy(3.tw)) {
                listOf(16 to 24, 20 to 24).forEach { (label, value) ->
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                        OnPrimaryBar(label.tw, 3.tw, KptTheme.shapes.extraSmall)
                        OnPrimaryBar(value.tw, 5.tw, KptTheme.shapes.extraSmall)
                    }
                }
            }
        }
        // 2×2 bordered h-24 tiles
        Grid(columns = 2, gap = 3.tw) {
            listOf(10 to (16 to 20), 8 to (14 to 24), 12 to (20 to 16), 11 to (16 to 20)).forEach { (tag, lines) ->
                add {
                    Outlined(Modifier.fillMaxWidth().height(24.tw), padding = 3.5f.tw) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                            Row(Modifier.fillMaxWidth()) {
                                Bar(7.tw, 7.tw, KptTheme.shapes.small)
                                Spacer(Modifier.weight(1f))
                                Bar(tag.tw, 3.5f.tw, KptTheme.shapes.extraSmall)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(1.tw)) {
                                Bar(lines.first.tw, 5.tw, KptTheme.shapes.extraSmall)
                                Bar(lines.second.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            }
                        }
                    }
                }
            }
        }
        // work queue: header bars, then a 3-column grid of six round task cards
        Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                Bar(28.tw, 4.tw, KptTheme.shapes.extraSmall)
                Bar(7.tw, 4.tw)
                Spacer(Modifier.weight(1f))
                Bar(12.tw, 3.tw, KptTheme.shapes.extraSmall)
            }
            Grid(columns = 3, gap = 2.tw) {
                listOf(12, 14, 10, 12, 14, 11).forEach { w ->
                    add {
                        Outlined(Modifier.fillMaxWidth(), padding = 2.5f.tw) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.tw)) {
                                Bar(8.tw, 8.tw)
                                Bar(6.tw, 4.tw, KptTheme.shapes.extraSmall)
                                Bar(w.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            }
                        }
                    }
                }
            }
        }
        // meetings: header bars, three bordered rows with a status pill and a dot
        Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Bar(32.tw, 4.tw, KptTheme.shapes.extraSmall)
                Spacer(Modifier.weight(1f))
                Bar(16.tw, 3.tw, KptTheme.shapes.extraSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
                listOf(32 to 20, 28 to 24, 36 to 16).forEach { (title, sub) ->
                    Outlined(Modifier.fillMaxWidth(), padding = 3.tw) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Bar(10.tw, 10.tw, KptTheme.shapes.small)
                            Column(Modifier.weight(1f).padding(start = 3.tw), verticalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                                Bar(title.tw, 3.5f.tw, KptTheme.shapes.extraSmall)
                                Bar(sub.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            }
                            Bar(12.tw, 5.tw)
                            Spacer(Modifier.width(2.tw))
                            Bar(4.tw, 4.tw)
                        }
                    }
                }
            }
        }
        // portfolio: one bordered card, header bars over a 3-column figure row
        Outlined(Modifier.fillMaxWidth(), padding = 3.5f.tw) {
            Column(verticalArrangement = Arrangement.spacedBy(3.tw)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Bar(36.tw, 4.tw, KptTheme.shapes.extraSmall)
                    Spacer(Modifier.weight(1f))
                    Bar(10.tw, 3.tw, KptTheme.shapes.extraSmall)
                }
                Row(Modifier.padding(top = 1.tw), horizontalArrangement = Arrangement.spacedBy(3.tw)) {
                    listOf(14 to 16, 16 to 20, 12 to 14).forEach { (label, value) ->
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5f.tw)) {
                            Bar(label.tw, 2.5f.tw, KptTheme.shapes.extraSmall)
                            Bar(value.tw, 4.tw, KptTheme.shapes.extraSmall)
                        }
                    }
                }
            }
        }
        // quick actions: a title bar over four h-9 chips
        Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
            Bar(24.tw, 3.5f.tw, KptTheme.shapes.extraSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(2.tw)) {
                repeat(4) { KptShimmerLoadingBox(Modifier.weight(1f).height(9.tw), shape = KptTheme.shapes.small) }
            }
        }
        // recently opened: header bars, two bordered rows with a round avatar
        Column(Modifier.padding(bottom = 2.tw), verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Bar(32.tw, 4.tw, KptTheme.shapes.extraSmall)
                Spacer(Modifier.weight(1f))
                Bar(14.tw, 3.tw, KptTheme.shapes.extraSmall)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.tw)) {
                listOf(28 to 16, 24 to 20).forEach { (title, sub) ->
                    Outlined(Modifier.fillMaxWidth(), padding = 2.5f.tw) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Bar(8.tw, 8.tw)
                            Column(Modifier.weight(1f).padding(start = 2.5f.tw), verticalArrangement = Arrangement.spacedBy(1.tw)) {
                                Bar(title.tw, 3.tw, KptTheme.shapes.extraSmall)
                                Bar(sub.tw, 2.tw, KptTheme.shapes.extraSmall)
                            }
                            Bar(10.tw, 3.tw, KptTheme.shapes.extraSmall)
                        }
                    }
                }
            }
        }
    }
}

/** One card refreshing on its own — its tiles shimmer under the real title. */
@Composable
private fun CardSkeleton(id: HomeCardId) {
    Column(Modifier.testTag(TestTags.Home.SKELETON), verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        val rows = when (id) {
            HomeCardId.MEETINGS -> 2
            HomeCardId.WORK_QUEUE -> 3
            else -> 2
        }
        if (id == HomeCardId.MEETINGS) {
            repeat(rows) { Outlined(Modifier.fillMaxWidth().height(16.tw), padding = 3.tw) { Bar(32.tw, 3.5f.tw, KptTheme.shapes.extraSmall) } }
        } else {
            Grid(columns = 2, gap = 2.5f.tw) {
                repeat(rows * 2) {
                    add {
                        Outlined(Modifier.fillMaxWidth().height(24.tw), padding = 3.5f.tw) {
                            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                Bar(7.tw, 7.tw, KptTheme.shapes.small)
                                Bar(16.tw, 5.tw, KptTheme.shapes.extraSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** `shimmer-light` bar; a null [width] fills the row. */
@Composable
private fun Bar(width: Dp?, height: Dp, shape: Shape = CircleShape) {
    KptShimmerLoadingBox((if (width == null) Modifier.fillMaxWidth() else Modifier.width(width)).height(height), shape = shape)
}

/** `shimmer-indigo` — the bars painted on the brand-coloured hero. */
@Composable
private fun OnPrimaryBar(width: Dp?, height: Dp, shape: Shape = CircleShape, fraction: Float = 1f) {
    val on = KptTheme.colorScheme.onPrimary
    KptShimmerLoadingBox(
        (if (width == null) Modifier.fillMaxWidth(fraction) else Modifier.width(width)).height(height),
        shape = shape,
        shimmerColor = on.copy(alpha = HERO_SHIMMER),
        highlightColor = on.copy(alpha = HERO_SHIMMER * 2),
    )
}

/** `bg-surface-white rounded-xl border border-border-subtle` — the skeleton's flat tile. */
@Composable
private fun Outlined(modifier: Modifier, padding: Dp, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(KptTheme.shapes.medium)
            .background(KptTheme.colorScheme.surfaceContainerLowest)
            .border(HAIRLINE, KptTheme.colorScheme.surfaceContainerHigh, KptTheme.shapes.medium)
            .padding(padding),
    ) { content() }
}

// ── Primitives ─────────────────────────────────────────────────────────────────────────────────

/** A Tailwind spacing unit (`p-3`, `w-11`, `gap-2.5` …) — one unit is the theme's `spacing.xs` (4 dp). */
private val Int.tw: Dp @Composable get() = KptTheme.spacing.xs * this
private val Float.tw: Dp @Composable get() = KptTheme.spacing.xs * this

/**
 * `bg-surface-card rounded-xl shadow-card` (or, [bordered], `bg-white border border-outline-variant
 * shadow-sm`) with [padding] inside. Clickable when [onClick] is given.
 */
@Composable
private fun Surface(
    modifier: Modifier = Modifier,
    padding: Dp = 0.tw,
    shape: Shape = KptTheme.shapes.medium,
    color: Color = KptTheme.colorScheme.surfaceContainerLowest,
    border: BorderStroke? = null,
    bordered: Boolean = false,
    elevation: Dp = CARD_SHADOW,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val edge = border ?: if (bordered) BorderStroke(HAIRLINE, KptTheme.colorScheme.surfaceContainerHigh) else null
    val colors = CardDefaults.cardColors(containerColor = color)
    val lift = CardDefaults.cardElevation(defaultElevation = elevation)
    val inner: @Composable () -> Unit = { Box(Modifier.padding(padding)) { content() } }
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = lift, border = edge) { inner() }
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = lift, border = edge) { inner() }
    }
}

@Composable
private fun IconBox(icon: ImageVector, tint: Color, glyph: Color, box: Dp, glyphSize: Dp, shape: Shape, modifier: Modifier = Modifier) {
    Box(modifier.size(box).background(tint, shape), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = glyph, modifier = Modifier.size(glyphSize))
    }
}

/** `px-2 py-0.5 rounded-full` label chip (optionally bordered / with a leading glyph). */
@Composable
private fun Chip(
    text: String,
    container: Color,
    content: Color,
    style: androidx.compose.ui.text.TextStyle,
    weight: FontWeight = FontWeight.SemiBold,
    shape: Shape = CircleShape,
    horizontal: Dp = 2.tw,
    vertical: Dp = 0.5f.tw,
    border: Color? = null,
    leading: ImageVector? = null,
) {
    Row(
        Modifier
            .clip(shape)
            .background(container)
            .then(if (border != null) Modifier.border(HAIRLINE, border, shape) else Modifier)
            .padding(horizontal = horizontal, vertical = vertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.tw),
    ) {
        leading?.let { Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(3.25f.tw)) }
        Text(text, style = style, fontWeight = weight, color = content, maxLines = 1)
    }
}

@Composable
private fun Figure(
    text: String,
    style: androidx.compose.ui.text.TextStyle = KptTheme.typography.bodyLarge,
    color: Color = KptTheme.colorScheme.onSurface,
    weight: FontWeight = FontWeight.Bold,
    modifier: Modifier = Modifier,
) = Text(text, modifier = modifier, style = style, fontWeight = weight, color = color)

/** Equal-width tiles, [columns] per row, [gap] both ways; a short last row keeps the tile width. */
@Composable
private fun Grid(columns: Int, gap: Dp, modifier: Modifier = Modifier, content: MutableList<@Composable () -> Unit>.() -> Unit) {
    val tiles = buildList(content)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        tiles.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { tile -> Box(Modifier.weight(1f).fillMaxHeight()) { tile() } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private const val NOON = 12
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val EVENING = 17
private const val HOURS_HALF_DAY = 12
private const val META_SEPARATOR = " · "
private const val META_DOT = "·"
private const val PERCENT_TENTHS = 1000
private const val MS_PER_MINUTE = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 1_440L

/**
 * Portfolio-at-risk benchmark the PAR tile is read against (Stitch: "tgt <5%"). PAR30 under 5% is the
 * common microfinance health line; one constant so an institution with a different policy changes one place.
 */
private const val PAR_TARGET = 0.05

// Tailwind opacity steps and tracking the markup uses (`/20`, `/80`, `tracking-wider` …).
private const val HERO_SUB = 0.9f // text-blue-100/90
private const val HERO_PILL = 0.2f // bg-white/20
private const val HERO_DISC = 0.1f // bg-white/10
private const val HERO_SHIMMER = 0.18f // shimmer-indigo
private const val TRACK_ALPHA = 0.2f // bg-black/20
private const val SKELETON_TRACK = 0.15f // bg-black/15
private const val SKELETON_RULE = 0.25f // border-indigo-400/25
private const val DRAFTS_FILL = 0.7f // bg-amber-50/70
private const val DRAFTS_EDGE = 0.3f // border-amber-200/60
private const val DRAFTS_BOX = 0.35f // bg-amber-200
private const val ERROR_FILL = 0.8f // bg-red-50/80 · bg-emerald-50/80
private const val EDGE_ALPHA = 0.25f // border-*-200
private const val SOFT_EDGE_ALPHA = 0.15f // border-rose-100 · border-red-100
private const val TILE_FILL = 0.4f // bg-surface-variant/40
private const val TINT_FILL = 0.5f // bg-red-50/50 · bg-amber-50/50
private const val OUTSTANDING_FILL = 0.4f // bg-primary-container/40
private const val INITIALS_FILL = 0.1f // bg-primary/10
private val TIGHT = (-0.025f).em // tracking-tight
private val WIDER = 0.05f.em // tracking-wider

private val CARD_SHADOW: Dp @Composable get() = KptTheme.elevation.level1 // shadow-card / shadow-sm
private val ELEVATED_SHADOW: Dp @Composable get() = KptTheme.elevation.level3 // shadow-elevated
private val HAIRLINE: Dp @Composable get() = KptTheme.spacing.xs / 4
