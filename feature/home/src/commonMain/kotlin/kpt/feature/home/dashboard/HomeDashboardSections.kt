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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.finance
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_card_error
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_open
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_summary
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_title
import kpt.feature.home.generated.resources.feature_home_dashboard_meetings_summary
import kpt.feature.home.generated.resources.feature_home_dashboard_meetings_title
import kpt.feature.home.generated.resources.feature_home_dashboard_portfolio_title
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_pending_badge
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_title
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_view_all
import kpt.feature.home.generated.resources.feature_home_dashboard_retry
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_active_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_active_loans
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_collected
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_outstanding
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_overdue
import kpt.feature.home.generated.resources.feature_home_dashboard_summary_par
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

internal val HomeCardId.title: StringResource
    get() = when (this) {
        HomeCardId.COLLECTIONS -> Res.string.feature_home_dashboard_collections_title
        HomeCardId.WORK_QUEUE -> Res.string.feature_home_dashboard_queue_title
        HomeCardId.MEETINGS -> Res.string.feature_home_dashboard_meetings_title
        HomeCardId.PORTFOLIO -> Res.string.feature_home_dashboard_portfolio_title
    }

internal val HomeCardId.tag: String
    get() = when (this) {
        HomeCardId.COLLECTIONS -> TestTags.Home.COLLECTIONS
        HomeCardId.WORK_QUEUE -> TestTags.Home.WORK_QUEUE
        HomeCardId.MEETINGS -> TestTags.Home.MEETINGS
        HomeCardId.PORTFOLIO -> TestTags.Home.PORTFOLIO
    }

@Composable
internal fun Section(
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.tw),
                ) {
                    Text(
                        stringResource(slot.id.title),
                        style = KptTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = c.onSurface,
                    )
                    if (data is HomeCard.WorkQueue && data.total + draftCount > 0) {
                        Chip(
                            stringResource(
                                Res.string.feature_home_dashboard_queue_pending_badge,
                                data.total + draftCount,
                            ),
                            container = c.tertiaryContainer,
                            content = c.onTertiaryContainer,
                            style = KptTheme.typography.labelMedium,
                        )
                    }
                }
                when {
                    failed -> Unit
                    slot.id != HomeCardId.MEETINGS && slot.id != HomeCardId.WORK_QUEUE -> sectionSummary(data)?.let {
                        Text(
                            it,
                            style = KptTheme.typography.labelMedium,
                            fontWeight = FontWeight.Normal,
                            color = c.onSurfaceVariant,
                            modifier = Modifier.padding(top = 1.tw),
                        )
                    }
                }
            }
            SectionTrailing(slot.id, data, businessDate, onNavigate)
        }
        // 03: "Couldn't load this card · ⟳ Retry" under the title — its own full-width row, so a long
        // trailing link can never squeeze it (it wrapped one letter per line on a large-font phone).
        if (failed) {
            SectionRetryLine(onRetry)
        }
        if (failedOverCache) {
            CardError(
                onRetry,
                error = content?.freshnessSignal?.lastError,
                cachedAt = content?.fetchedAt,
            )
        }
        card()
    }
}

/** The trailing action beside a section title — open the sheet, view the inbox, or the meetings count. */
@Composable
private fun SectionTrailing(
    id: HomeCardId,
    data: HomeCard?,
    businessDate: String?,
    onNavigate: (HomeNavigation) -> Unit,
) {
    val c = KptTheme.colorScheme
    when {
        // In every state the card shows — the sheet opens even when this summary failed to load.
        id == HomeCardId.COLLECTIONS -> LinkAction(
            stringResource(Res.string.feature_home_dashboard_collections_open),
            chevron = true,
            modifier = Modifier.testTag(TestTags.Home.OPEN_COLLECTION_SHEET),
        ) { onNavigate(HomeNavigation.CollectionSheet) }
        id == HomeCardId.WORK_QUEUE && data is HomeCard.WorkQueue &&
            data.checkerTasks != null -> LinkAction(
            stringResource(Res.string.feature_home_dashboard_queue_view_all),
            modifier = Modifier.testTag(TestTags.Home.QUEUE_VIEW_ALL),
        ) { onNavigate(HomeNavigation.CheckerInbox) }
        id == HomeCardId.MEETINGS && data is HomeCard.Meetings &&
            data.today.isNotEmpty() && businessDate != null -> Text(
            stringResource(
                Res.string.feature_home_dashboard_meetings_summary,
                data.today.size,
                businessDate.shortDate(),
            ),
            style = KptTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = c.onSurfaceVariant,
        )
    }
}

/** 03: "Couldn't load this card · ⟳ Retry" — the failed card's own retry line. */
@Composable
private fun SectionRetryLine(onRetry: () -> Unit) {
    val c = KptTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.5f.tw),
        modifier = Modifier.padding(top = 0.5f.tw),
    ) {
        Text(
            stringResource(Res.string.feature_home_dashboard_card_error),
            style = KptTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.finance.moneyNegative,
        )
        Text(META_DOT, style = KptTheme.typography.labelMedium, color = c.onSurfaceVariant)
        Row(
            modifier = Modifier.heightIn(
                min = 8.tw,
            ).clip(
                KptTheme.shapes.extraSmall,
            ).clickable(role = Role.Button, onClick = onRetry).padding(horizontal = 1.tw),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.5f.tw),
        ) {
            Icon(
                AppIcons.Refresh,
                contentDescription = null,
                tint = c.primary,
                modifier = Modifier.size(3.5f.tw),
            )
            Text(
                stringResource(Res.string.feature_home_dashboard_retry),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = c.primary,
            )
        }
    }
}

/** 03: `bg-white rounded-2xl p-4 border shadow-sm space-y-3` around a section — or just the column. */
@Composable
internal fun SectionCard(carded: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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
internal fun LinkAction(text: String, modifier: Modifier = Modifier, chevron: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = modifier.heightIn(
            min = 12.tw,
        ).clip(KptTheme.shapes.extraSmall).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 1.tw),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.5f.tw),
    ) {
        Text(
            text,
            style = KptTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.primary,
        )
        if (chevron) {
            Icon(
                AppIcons.ChevronRight,
                contentDescription = null,
                tint = KptTheme.colorScheme.primary,
                modifier = Modifier.size(4.tw),
            )
        }
    }
}

@Composable
internal fun sectionSummary(data: HomeCard?): String? = when (data) {
    is HomeCard.Collections -> listOfNotNull(
        pluralStringResource(
            Res.plurals.feature_home_dashboard_collections_summary,
            data.clientsDue,
            data.clientsDue,
            data.dueByCurrency.joinToString(
                ", ",
            ) {
                money(it.amount, it.currencyCode, 0)
            },
        ),
        data.collectedByCurrency.takeIf {
            it.isNotEmpty()
        }?.let { m ->
            stringResource(
                Res.string.feature_home_dashboard_summary_collected,
                m.joinToString(", ") {
                    money(it.amount, it.currencyCode, 0)
                },
            )
        },
        data.overdueClients?.let { stringResource(Res.string.feature_home_dashboard_summary_overdue, it) },
    ).joinToString(META_SEPARATOR).takeIf { data.clientsDue > 0 || data.collectedByCurrency.isNotEmpty() }
    is HomeCard.Portfolio -> listOfNotNull(
        data.activeClients?.let { stringResource(Res.string.feature_home_dashboard_summary_active_clients, it) },
        data.activeLoans?.let { stringResource(Res.string.feature_home_dashboard_summary_active_loans, it) },
        data.loanTotals?.takeIf {
            it.size == 1
        }?.first()?.let {
            stringResource(
                Res.string.feature_home_dashboard_summary_outstanding,
                money(it.outstanding, it.currencyCode, 0),
            )
        },
        data.loanTotals?.takeIf {
            it.size == 1
        }?.first()?.par?.let { stringResource(Res.string.feature_home_dashboard_summary_par, percent(it)) },
    ).takeIf { it.isNotEmpty() }?.joinToString(META_SEPARATOR)
    else -> null
}

internal fun money(amount: Double, code: String, decimals: Int = 2): String = CurrencyFormatter.format(
    amount,
    code,
    decimals,
)

internal fun percent(fraction: Double): String = "${(fraction * PERCENT_TENTHS).toLong() / 10.0}%"

// ── 02 #collections_card ───────────────────────────────────────────────────────────────────────
