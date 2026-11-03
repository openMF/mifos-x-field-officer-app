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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.PendingActions
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AccentColors
import kpt.core.designsystem.theme.finance
import kpt.core.model.dashboard.CurrencyTotals
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_clients_value
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_collected
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_collected_today
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_due_amount
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_due_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_empty
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_goal
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_of_goal
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_overdue
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_overdue_chip
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_percent_collected
import kpt.feature.home.generated.resources.feature_home_dashboard_collections_remains
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_center_groups
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_on_schedule
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_today
import kpt.feature.home.generated.resources.feature_home_dashboard_meeting_upcoming
import kpt.feature.home.generated.resources.feature_home_dashboard_meetings_empty
import kpt.feature.home.generated.resources.feature_home_dashboard_portfolio_total_outstanding
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_approval
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_checker
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_disbursal
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_drafts
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_pending_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_reschedules
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_active_clients
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_active_loans
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_centers
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_groups
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_in_arrears
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_overdue
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_par
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_par_target
import kpt.feature.home.generated.resources.feature_home_dashboard_tile_savings
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
internal fun CollectionsBody(data: HomeCard.Collections) = Column(verticalArrangement = Arrangement.spacedBy(3.tw)) {
    if (data.clientsDue == 0 && data.collectedByCurrency.isEmpty()) {
        Text(
            stringResource(Res.string.feature_home_dashboard_collections_empty),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
        return@Column
    }
    // One hero per currency the day touches (Stitch draws one; a mixed-currency day must not sum across).
    val codes = (data.dueByCurrency.map { it.currencyCode } + data.collectedByCurrency.map { it.currencyCode })
        .distinct()
    codes.forEach { code -> CollectedHero(code, data) }
    val c = KptTheme.colorScheme
    val f = MaterialTheme.finance
    // grid grid-cols-2 gap-3 pt-1
    Grid(columns = 2, gap = 3.tw, modifier = Modifier.padding(top = 1.tw)) {
        add {
            StatTile(
                stringResource(Res.string.feature_home_dashboard_collections_due_clients),
                AppIcons.Groups,
                c.primaryContainer,
                c.primary,
            ) {
                val text =
                    pluralStringResource(
                        Res.plurals.feature_home_dashboard_collections_clients_value,
                        data.clientsDue,
                        data.clientsDue,
                    )
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
                        withStyle(
                            SpanStyle(
                                fontSize = KptTheme.typography.labelMedium.fontSize,
                                fontWeight = FontWeight.Normal,
                                color = unit,
                            ),
                        ) {
                            append(text.substring(0, at))
                        }
                        append(n)
                        withStyle(
                            SpanStyle(
                                fontSize = KptTheme.typography.labelMedium.fontSize,
                                fontWeight = FontWeight.Normal,
                                color = unit,
                            ),
                        ) {
                            append(text.substring(at + n.length))
                        }
                    },
                    style = KptTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = c.onSurface,
                )
            }
        }
        add {
            StatTile(
                stringResource(Res.string.feature_home_dashboard_collections_due_amount),
                Icons.AutoMirrored.Outlined.ReceiptLong,
                c.primaryContainer,
                c.primary,
            ) {
                data.dueByCurrency.forEach {
                    Figure(
                        money(it.amount, it.currencyCode, 0),
                        style = KptTheme.typography.titleLarge,
                        color = c.onSurface,
                    )
                }
            }
        }
        add {
            StatTile(
                stringResource(Res.string.feature_home_dashboard_collections_collected),
                Icons.Outlined.CheckCircle,
                c.secondaryContainer,
                c.secondary,
            ) {
                val collected = data.collectedByCurrency.ifEmpty { data.dueByCurrency.map { it.copy(amount = 0.0) } }
                collected.forEach {
                    Figure(
                        money(it.amount, it.currencyCode, 0),
                        style = KptTheme.typography.titleLarge,
                        color = f.moneyPositive,
                    )
                }
            }
        }
        data.overdueClients?.let { overdue ->
            add {
                StatTile(
                    stringResource(Res.string.feature_home_dashboard_collections_overdue),
                    Icons.Outlined.Warning,
                    f.moneyNegativeContainer,
                    f.moneyNegative,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.tw),
                    ) {
                        Figure("$overdue", style = KptTheme.typography.titleLarge, color = f.moneyNegative)
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
internal fun CollectedHero(code: String, data: HomeCard.Collections) {
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
            .background(
                Brush.linearGradient(listOf(AccentColors.heroStart, AccentColors.heroMid, AccentColors.heroEnd)),
            )
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
                Row(
                    Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.5f.tw),
                ) {
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
                Text(
                    money(collected, code),
                    style = KptTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = TIGHT,
                    color = on,
                )
                Row(Modifier.padding(top = 1.tw)) {
                    Text(
                        stringResource(Res.string.feature_home_dashboard_collections_goal, money(goal, code)),
                        modifier = Modifier.weight(1f),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        color = AccentColors.heroText.copy(alpha = HERO_SUB),
                    )
                    Text(
                        stringResource(Res.string.feature_home_dashboard_collections_remains, money(due, code)),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = AccentColors.heroRemains,
                    )
                }
            }
            // h-2.5 bg-black/20 rounded-full p-0.5 · fill from-emerald-300 to-emerald-400
            Column(verticalArrangement = Arrangement.spacedBy(1.tw)) {
                Box(
                    Modifier.fillMaxWidth().height(
                        2.5f.tw,
                    ).clip(CircleShape).background(Color.Black.copy(alpha = TRACK_ALPHA)).padding(0.5f.tw),
                ) {
                    Box(
                        Modifier.fillMaxWidth(fraction).fillMaxHeight().clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(listOf(AccentColors.heroFillStart, AccentColors.heroFillEnd)),
                            ),
                    )
                }
                Row {
                    val label = KptTheme.typography.labelSmall
                    Text(money(0.0, code), style = label, color = AccentColors.heroFaint)
                    Text(
                        stringResource(Res.string.feature_home_dashboard_collections_percent_collected, pct),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = label,
                        color = AccentColors.heroFaint,
                    )
                    Text(money(goal, code), style = label, color = AccentColors.heroFaint)
                }
            }
        }
    }
}

/** `bg-surface-card rounded-xl p-3.5 shadow-card` — label + w-7 icon box over a text-xl figure. */
@Composable
internal fun StatTile(label: String, icon: ImageVector, tint: Color, glyph: Color, value: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), padding = 3.5f.tw) {
        Column {
            Row(Modifier.padding(bottom = 2.tw), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                IconBox(icon, tint, glyph, box = 7.tw, glyphSize = 4.tw, shape = KptTheme.shapes.small)
            }
            value()
        }
    }
}

// ── 02 #work_queue_card ────────────────────────────────────────────────────────────────────────

@Composable
internal fun WorkQueueBody(data: HomeCard.WorkQueue, draftCount: Int, onNavigate: (HomeNavigation) -> Unit) {
    val c = KptTheme.colorScheme
    // grid grid-cols-2 gap-2.5 — each tile carries its own Stitch tint
    Grid(columns = 2, gap = 2.5f.tw) {
        data.checkerTasks?.let { n ->
            add {
                QueueTile(
                    Res.string.feature_home_dashboard_queue_checker,
                    n,
                    Icons.AutoMirrored.Outlined.FactCheck,
                    c.primaryContainer,
                    c.primary,
                    TestTags.Home.QUEUE_CHECKER,
                ) {
                    onNavigate(HomeNavigation.CheckerInbox)
                }
            }
        }
        data.awaitingApproval?.let { n ->
            add {
                QueueTile(
                    Res.string.feature_home_dashboard_queue_approval,
                    n,
                    Icons.Outlined.PendingActions,
                    c.tertiaryContainer,
                    c.tertiary,
                    TestTags.Home.QUEUE_APPROVAL,
                ) {
                    onNavigate(HomeNavigation.LoanQueue(DashboardRepository.STATUS_SUBMITTED))
                }
            }
        }
        data.awaitingDisbursal?.let { n ->
            add {
                QueueTile(
                    Res.string.feature_home_dashboard_queue_disbursal,
                    n,
                    Icons.Outlined.Verified,
                    c.secondaryContainer,
                    c.secondary,
                    TestTags.Home.QUEUE_DISBURSAL,
                ) {
                    onNavigate(HomeNavigation.LoanQueue(DashboardRepository.STATUS_APPROVED))
                }
            }
        }
        data.pendingClients?.let { n ->
            add {
                QueueTile(
                    Res.string.feature_home_dashboard_queue_pending_clients,
                    n,
                    Icons.Outlined.HowToReg,
                    AccentColors.blueContainer,
                    AccentColors.blue,
                    TestTags.Home.QUEUE_PENDING_CLIENTS,
                ) {
                    onNavigate(HomeNavigation.PendingClients)
                }
            }
        }
        data.pendingReschedules?.let { n ->
            add {
                QueueTile(
                    Res.string.feature_home_dashboard_queue_reschedules,
                    n,
                    Icons.Outlined.EventRepeat,
                    AccentColors.purpleContainer,
                    AccentColors.purple,
                    TestTags.Home.QUEUE_RESCHEDULES,
                ) {
                    onNavigate(HomeNavigation.Reschedules)
                }
            }
        }
        // Stitch 02: the sixth tile is always there — "0" says there is nothing waiting to sync.
        add { DraftsTile(draftCount) { onNavigate(HomeNavigation.Drafts) } }
    }
}

/** `bg-surface-card rounded-xl p-3 shadow-card` — w-7 icon box + text-xs label, text-sm bold count top-right. */
@Composable
internal fun QueueTile(
    label: StringResource,
    count: Int,
    icon: ImageVector,
    tint: Color,
    glyph: Color,
    tag: String,
    onClick: () -> Unit,
) {
    Surface(Modifier.fillMaxSize().testTag(tag), padding = 3.tw, onClick = onClick) {
        Row {
            Column(Modifier.weight(1f)) {
                IconBox(icon, tint, glyph, box = 7.tw, glyphSize = 4.tw, shape = KptTheme.shapes.small)
                Spacer(Modifier.height(3.tw))
                Text(
                    stringResource(label),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = KptTheme.colorScheme.onSurface,
                )
            }
            Text(
                "$count",
                style = KptTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = KptTheme.colorScheme.onSurface,
            )
        }
    }
}

/** The amber tile: `bg-amber-50/70 border-amber-200/60`, an amber-200 icon box, count on a tertiary badge. */
@Composable
internal fun DraftsTile(count: Int, onClick: () -> Unit) {
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
                IconBox(
                    Icons.Outlined.SyncProblem,
                    lerp(c.tertiaryContainer, c.tertiary, DRAFTS_BOX),
                    c.onTertiaryContainer,
                    box = 7.tw,
                    glyphSize = 4.tw,
                    shape = KptTheme.shapes.small,
                )
                Spacer(Modifier.height(3.tw))
                Text(
                    stringResource(Res.string.feature_home_dashboard_queue_drafts),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = c.onTertiaryContainer,
                )
            }
            Chip(
                "$count",
                container = c.tertiary,
                content = c.onTertiary,
                style = KptTheme.typography.labelMedium,
                weight = FontWeight.Bold,
                shape = KptTheme.shapes.extraSmall,
                horizontal = 1.5f.tw,
            )
        }
    }
}

// ── 02 #meetings_card ──────────────────────────────────────────────────────────────────────────

/** "10:00 AM" from the calendar's "HH:mm". */
internal fun String.clock12(): String {
    val (h, m) = split(':').map { it.toIntOrNull() ?: 0 }
    val hour = (h + HOURS_HALF_DAY - 1) % HOURS_HALF_DAY + 1
    return "${hour.toString().padStart(
        2,
        '0',
    )}:${m.toString().padStart(2, '0')} ${if (h < HOURS_HALF_DAY) "AM" else "PM"}"
}

@Composable
internal fun MeetingsBody(data: HomeCard.Meetings, onNavigate: (HomeNavigation) -> Unit) {
    val c = KptTheme.colorScheme
    if (data.today.isEmpty()) {
        Text(
            stringResource(Res.string.feature_home_dashboard_meetings_empty),
            style = KptTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
        )
        return
    }
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    val nowHm = "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
    Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        data.today.forEach { meeting ->
            // On schedule once the meeting time has come; Upcoming before it; Today when no time is set.
            val started = meeting.time?.let { it <= nowHm }
            Surface(Modifier.fillMaxWidth().testTag(TestTags.Home.MEETING_ROW), padding = 3.5f.tw, onClick = {
                onNavigate(HomeNavigation.Center(meeting.centerId))
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.tw)) {
                        IconBox(
                            Icons.Outlined.MeetingRoom,
                            c.primaryContainer,
                            c.primary,
                            box = 10.tw,
                            glyphSize = 6.tw,
                            shape = KptTheme.shapes.small,
                            modifier = Modifier.padding(top = 0.5f.tw),
                        )
                        Column {
                            Text(
                                meeting.name,
                                style = KptTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = c.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                stringResource(Res.string.feature_home_dashboard_meeting_center_groups, meeting.groups),
                                style = KptTheme.typography.labelMedium,
                                fontWeight = FontWeight.Normal,
                                color = c.onSurfaceVariant,
                            )
                            meeting.time?.let { t ->
                                val tone = if (started == true) c.primary else c.onSurfaceVariant
                                Row(
                                    Modifier.padding(top = 1.tw),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(1.tw),
                                ) {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = tone,
                                        modifier = Modifier.size(3.5f.tw),
                                    )
                                    Text(
                                        t.clock12(),
                                        style = KptTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = tone,
                                    )
                                }
                            }
                        }
                    }
                    // px-2.5 py-1 text-[11px] font-semibold rounded-full
                    when (started) {
                        true -> Chip(
                            stringResource(Res.string.feature_home_dashboard_meeting_on_schedule),
                            c.secondaryContainer,
                            c.secondary,
                            KptTheme.typography.labelSmall,
                            horizontal = 2.5f.tw,
                            vertical = 1.tw,
                            border = c.secondary.copy(alpha = EDGE_ALPHA),
                        )
                        false -> Chip(
                            stringResource(Res.string.feature_home_dashboard_meeting_upcoming),
                            c.surfaceContainer,
                            c.onSurfaceVariant,
                            KptTheme.typography.labelSmall,
                            horizontal = 2.5f.tw,
                            vertical = 1.tw,
                        )
                        null -> Chip(
                            stringResource(Res.string.feature_home_dashboard_meeting_today),
                            c.primaryContainer,
                            c.primary,
                            KptTheme.typography.labelSmall,
                            horizontal = 2.5f.tw,
                            vertical = 1.tw,
                        )
                    }
                }
            }
        }
    }
}

// ── 02 #portfolio_card ─────────────────────────────────────────────────────────────────────────

@Composable
internal fun PortfolioBody(data: HomeCard.Portfolio) {
    val c = KptTheme.colorScheme
    val f = MaterialTheme.finance
    val totals = data.loanTotals?.takeIf { it.isNotEmpty() }
    val plain = c.surfaceContainer.copy(alpha = TILE_FILL) to c.surfaceContainerHigh
    // grid grid-cols-2 gap-2.5 — the outstanding tile spans both columns (03 #portfolio_card)
    Column(verticalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        Pair(
            data.activeClients?.let { n ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_active_clients, plain) { Figure("$n") }
                }
            },
            data.activeLoans?.let { n ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_active_loans, plain) { Figure("$n") }
                }
            },
        ).Row2()
        totals?.let { t ->
            // col-span-2 p-3 bg-primary-container/40 border-indigo-200 — label + bank glyph, text-xl extrabold
            OutstandingTile(t)
        }
        Pair(
            totals?.let { t ->
                @Composable {
                    PortfolioTile(
                        Res.string.feature_home_dashboard_tile_overdue,
                        f.moneyNegativeContainer.copy(alpha = TINT_FILL) to
                            f.moneyNegative.copy(alpha = SOFT_EDGE_ALPHA),
                        labelColor = f.moneyNegative,
                    ) {
                        t.forEach { Figure(money(it.overdue, it.currencyCode, 0), color = f.moneyNegative) }
                    }
                }
            },
            data.inArrears?.let { n ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_in_arrears, plain) { Figure("$n") }
                }
            },
        ).Row2()
        Pair(
            totals?.let { t ->
                @Composable {
                    // "PAR · tgt <5%" — amber tile above the benchmark, the healthy green figure within it
                    ParTile(t)
                }
            },
            data.savings?.takeIf {
                it.isNotEmpty()
            }?.let { sv ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_savings, plain) {
                        sv.forEach { Figure(money(it.amount, it.currencyCode, 0), color = f.moneyPositive) }
                    }
                }
            },
        ).Row2()
        Pair(
            data.groups?.let { n ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_groups, plain) { Figure("$n") }
                }
            },
            data.centers?.let { n ->
                @Composable {
                    PortfolioTile(Res.string.feature_home_dashboard_tile_centers, plain) { Figure("$n") }
                }
            },
        ).Row2()
    }
}

/** col-span-2 bg-primary-container/40 — total outstanding per currency under a bank glyph (03 #portfolio_card). */
@Composable
private fun OutstandingTile(t: List<CurrencyTotals>) {
    val c = KptTheme.colorScheme
    Box(
        Modifier.fillMaxWidth().clip(
            KptTheme.shapes.medium,
        ).background(c.primaryContainer.copy(alpha = OUTSTANDING_FILL))
            .border(HAIRLINE, c.primary.copy(alpha = EDGE_ALPHA), KptTheme.shapes.medium).padding(3.tw),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.feature_home_dashboard_portfolio_total_outstanding),
                    Modifier.weight(1f),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = c.primary,
                )
                Icon(
                    AppIcons.AccountBalance,
                    contentDescription = null,
                    tint = c.primary,
                    modifier = Modifier.size(4.5f.tw),
                )
            }
            t.forEach {
                Figure(
                    money(it.outstanding, it.currencyCode),
                    modifier = Modifier.padding(top = 0.5f.tw),
                    style = KptTheme.typography.titleLarge,
                    color = c.onPrimaryContainer,
                    weight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

/** "PAR · tgt <5%" — amber tile above the benchmark, the healthy green figure within it. */
@Composable
private fun ParTile(t: List<CurrencyTotals>) {
    val c = KptTheme.colorScheme
    val f = MaterialTheme.finance
    PortfolioTile(
        Res.string.feature_home_dashboard_tile_par,
        c.tertiaryContainer.copy(alpha = TINT_FILL) to c.tertiary.copy(alpha = EDGE_ALPHA),
        labelColor = c.tertiary,
        note = stringResource(Res.string.feature_home_dashboard_tile_par_target, percent(PAR_TARGET)),
    ) {
        t.forEach { x ->
            val par = x.par ?: 0.0
            Figure(
                if (t.size > 1) "${percent(par)} ${x.currencyCode}" else percent(par),
                color = if (par > PAR_TARGET) c.tertiary else f.moneyPositive,
            )
        }
    }
}

/** Two equal tiles in a row (gap-2.5); a missing one leaves its half empty, none renders nothing. */
@Composable
internal fun Pair<(@Composable () -> Unit)?, (@Composable () -> Unit)?>.Row2() {
    if (first == null && second == null) return
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(2.5f.tw)) {
        listOf(first, second).forEach { tile -> Box(Modifier.weight(1f).fillMaxHeight()) { tile?.invoke() } }
    }
}

/** 03: `p-2.5 rounded-xl border` on a soft fill — text-[11px] label (+ optional note) over a text-base bold figure. */
@Composable
internal fun PortfolioTile(
    label: StringResource,
    colors: Pair<Color, Color>,
    labelColor: Color = KptTheme.colorScheme.onSurfaceVariant,
    note: String? = null,
    value: @Composable () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().clip(KptTheme.shapes.medium).background(colors.first)
            .border(HAIRLINE, colors.second, KptTheme.shapes.medium).padding(2.5f.tw),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(label),
                    Modifier.weight(1f),
                    style = KptTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = labelColor,
                )
                note?.let {
                    Text(
                        it,
                        style = KptTheme.typography.labelSmall,
                        fontWeight = FontWeight.Normal,
                        color = KptTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            value()
        }
    }
}

// ── #quick_actions (02 content · 04 error · 05 offline) ────────────────────────────────────────
