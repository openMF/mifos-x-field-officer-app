/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.notification.AppNotification
import kpt.feature.notification.generated.resources.Res
import kpt.feature.notification.generated.resources.feature_notification_empty_body
import kpt.feature.notification.generated.resources.feature_notification_empty_title
import kpt.feature.notification.generated.resources.feature_notification_mark_all_read
import kpt.feature.notification.generated.resources.feature_notification_mark_all_read_failed
import kpt.feature.notification.generated.resources.feature_notification_object_center
import kpt.feature.notification.generated.resources.feature_notification_object_client
import kpt.feature.notification.generated.resources.feature_notification_object_group
import kpt.feature.notification.generated.resources.feature_notification_object_loan
import kpt.feature.notification.generated.resources.feature_notification_object_savings
import kpt.feature.notification.generated.resources.feature_notification_section_earlier
import kpt.feature.notification.generated.resources.feature_notification_section_new
import kpt.feature.notification.generated.resources.feature_notification_time_days_ago
import kpt.feature.notification.generated.resources.feature_notification_time_hours_ago
import kpt.feature.notification.generated.resources.feature_notification_time_just_now
import kpt.feature.notification.generated.resources.feature_notification_time_minutes_ago
import kpt.feature.notification.generated.resources.feature_notification_time_yesterday
import kpt.feature.notification.generated.resources.feature_notification_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
internal fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onOpen: (AppNotification) -> Unit,
    viewModel: NotificationsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    EventsEffect(eventFlow = viewModel.eventFlow) { event ->
        when (event) {
            NotificationsEvent.NavigateBack -> onNavigateBack()
            is NotificationsEvent.Open -> onOpen(event.notification)
            NotificationsEvent.MarkAllReadFailed ->
                snackbar.showSnackbar(getString(Res.string.feature_notification_mark_all_read_failed))
        }
    }
    NotificationsScreenContent(
        notifications = notifications,
        marking = state.marking,
        onAction = viewModel::trySendAction,
        snackbarHostState = snackbar,
    )
}

@Composable
internal fun NotificationsScreenContent(
    notifications: ScreenState<List<AppNotification>>,
    marking: Boolean,
    onAction: (NotificationsAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val unread = (notifications as? ScreenState.Content)?.data.orEmpty().count { !it.isRead }
    AppScaffold(
        title = stringResource(Res.string.feature_notification_title),
        onBackPressed = { onAction(NotificationsAction.NavigateBack) },
        snackbarHostState = snackbarHostState,
        actions = {
            // Fineract can only mark ALL read, so the action shows only when there is something unread.
            if (unread > 0) {
                TextButton(
                    onClick = { onAction(NotificationsAction.MarkAllRead) },
                    enabled = !marking,
                    modifier = Modifier.testTag(NotificationsTestTags.MARK_ALL_READ),
                ) { Text(stringResource(Res.string.feature_notification_mark_all_read)) }
            }
        },
        modifier = modifier.testTag(NotificationsTestTags.SCREEN),
    ) { padding ->
        RefreshableScreenContent(
            state = notifications,
            onRefresh = { onAction(NotificationsAction.Refresh) },
            modifier = Modifier.fillMaxSize().padding(padding),
            empty = { EmptyNotifications() },
        ) { list, _ ->
            if (list.isEmpty()) {
                EmptyNotifications()
            } else {
                NotificationList(list, onOpen = { onAction(NotificationsAction.Open(it)) })
            }
        }
    }
}

@Composable
private fun NotificationList(list: List<AppNotification>, onOpen: (AppNotification) -> Unit) {
    val sp = KptTheme.spacing
    val (fresh, earlier) = list.partition { !it.isRead }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag(NotificationsTestTags.LIST),
        contentPadding = PaddingValues(horizontal = sp.md, vertical = sp.sm),
        verticalArrangement = Arrangement.spacedBy(sp.sm),
    ) {
        if (fresh.isNotEmpty()) {
            item(key = "new") { SectionHeader(Res.string.feature_notification_section_new) }
            items(fresh, key = { it.id }) { NotificationRow(it, onOpen) }
        }
        if (earlier.isNotEmpty()) {
            item(key = "earlier") { SectionHeader(Res.string.feature_notification_section_earlier) }
            items(earlier, key = { it.id }) { NotificationRow(it, onOpen) }
        }
    }
}

@Composable
private fun SectionHeader(title: StringResource) {
    Text(
        stringResource(title),
        modifier = Modifier.padding(top = KptTheme.spacing.sm),
        style = KptTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = KptTheme.colorScheme.onSurfaceVariant,
    )
}

/** Tinted icon box · the server's sentence (bold while unread) · "Loan #1439 · 6d ago" · unread dot. */
@Composable
private fun NotificationRow(notification: AppNotification, onOpen: (AppNotification) -> Unit) {
    val c = KptTheme.colorScheme
    val sp = KptTheme.spacing
    val (tint, glyph, icon) = notification.style()
    val colors = CardDefaults.cardColors(containerColor = c.surfaceContainerLowest)
    val elevation = CardDefaults.cardElevation(defaultElevation = KptTheme.elevation.level1)
    val body: @Composable () -> Unit = {
        Row(
            Modifier.fillMaxWidth().padding(sp.sm + sp.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(sp.sm + sp.xs),
        ) {
            Box(Modifier.size(ICON_BOX).background(tint, KptTheme.shapes.small), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = glyph, modifier = Modifier.size(sp.md + sp.xs))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sp.xs / 2)) {
                Text(
                    notification.content,
                    style = KptTheme.typography.titleSmall,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = c.onSurface,
                )
                Text(
                    listOfNotNull(notification.recordLabel(), ago(notification.createdAt)).joinToString(META_SEPARATOR),
                    style = KptTheme.typography.labelMedium,
                    color = c.onSurfaceVariant,
                )
            }
            if (!notification.isRead) {
                Box(Modifier.size(sp.sm).background(c.primary, CircleShape).testTag(NotificationsTestTags.UNREAD_DOT))
            }
        }
    }
    val modifier = Modifier.fillMaxWidth().testTag(NotificationsTestTags.ROW)
    if (notification.opensRecord) {
        Card(
            onClick = { onOpen(notification) },
            modifier = modifier,
            shape = KptTheme.shapes.medium,
            colors = colors,
            elevation = elevation,
        ) { body() }
    } else {
        Card(modifier = modifier, shape = KptTheme.shapes.medium, colors = colors, elevation = elevation) { body() }
    }
}

@Composable
private fun EmptyNotifications() {
    val sp = KptTheme.spacing
    Column(
        Modifier.fillMaxSize().padding(sp.xl).testTag(NotificationsTestTags.EMPTY),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(sp.sm, Alignment.CenterVertically),
    ) {
        Box(
            Modifier.size(EMPTY_BOX).background(KptTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.Notifications,
                contentDescription = null,
                tint = KptTheme.colorScheme.primary,
                modifier = Modifier.size(sp.lg + sp.xs),
            )
        }
        Text(
            stringResource(Res.string.feature_notification_empty_title),
            style = KptTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.feature_notification_empty_body),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private data class RowStyle(val tint: Color, val glyph: Color, val icon: ImageVector)

@Composable
private fun AppNotification.style(): RowStyle {
    val c = KptTheme.colorScheme
    return when (target) {
        NotificationTarget.LOAN -> RowStyle(c.tertiaryContainer, c.tertiary, AppIcons.Payments)
        NotificationTarget.CLIENT -> RowStyle(c.primaryContainer, c.primary, AppIcons.Person)
        NotificationTarget.GROUP -> RowStyle(c.secondaryContainer, c.secondary, AppIcons.Groups)
        NotificationTarget.CENTER -> RowStyle(c.secondaryContainer, c.secondary, AppIcons.HomeWork)
        NotificationTarget.SAVINGS -> RowStyle(c.primaryContainer, c.primary, AppIcons.PiggyBank)
        null -> RowStyle(c.surfaceContainer, c.onSurfaceVariant, AppIcons.Notifications)
    }
}

@Composable
private fun AppNotification.recordLabel(): String? {
    val id = objectId
    val label = when (target) {
        NotificationTarget.LOAN -> Res.string.feature_notification_object_loan
        NotificationTarget.CLIENT -> Res.string.feature_notification_object_client
        NotificationTarget.GROUP -> Res.string.feature_notification_object_group
        NotificationTarget.CENTER -> Res.string.feature_notification_object_center
        NotificationTarget.SAVINGS -> Res.string.feature_notification_object_savings
        null -> null
    }
    return if (id == null || label == null) null else stringResource(label, id.toString())
}

/** "Just now" / "12m ago" / "3h ago" / "Yesterday" / "6d ago" from the server's ISO local date-time. */
@Composable
private fun ago(createdAt: String): String? {
    val at = runCatching {
        LocalDateTime.parse(createdAt).toInstant(TimeZone.currentSystemDefault())
    }.getOrNull() ?: return null
    val minutes = ((Clock.System.now() - at).inWholeMinutes).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(Res.string.feature_notification_time_just_now)
        minutes < MINUTES_PER_HOUR -> stringResource(Res.string.feature_notification_time_minutes_ago, minutes.toInt())
        minutes < MINUTES_PER_DAY ->
            stringResource(Res.string.feature_notification_time_hours_ago, (minutes / MINUTES_PER_HOUR).toInt())
        minutes < 2 * MINUTES_PER_DAY -> stringResource(Res.string.feature_notification_time_yesterday)
        else -> stringResource(Res.string.feature_notification_time_days_ago, (minutes / MINUTES_PER_DAY).toInt())
    }
}

internal object NotificationsTestTags {
    const val SCREEN = "notification_screen"
    const val LIST = "notification_list"
    const val ROW = "notification_row"
    const val UNREAD_DOT = "notification_unread_dot"
    const val EMPTY = "notification_empty"
    const val MARK_ALL_READ = "notification_mark_all_read"
}

private const val META_SEPARATOR = " · "
private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 1_440L

// 40 dp row icon box and 64 dp empty-state badge, composed from the spacing scale.
private val ICON_BOX: Dp @Composable get() = KptTheme.spacing.xl + KptTheme.spacing.sm
private val EMPTY_BOX: Dp @Composable get() = KptTheme.spacing.xxl
