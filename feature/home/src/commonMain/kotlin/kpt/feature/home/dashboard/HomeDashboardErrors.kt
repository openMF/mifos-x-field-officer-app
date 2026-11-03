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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.error.ErrorCategory
import kpt.core.base.store.error.categorize
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.finance
import kpt.core.store.config.rememberAppErrorMessageFor
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_card_cached
import kpt.feature.home.generated.resources.feature_home_dashboard_card_error
import kpt.feature.home.generated.resources.feature_home_dashboard_card_error_body
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
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_banner_body
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_banner_title
import kpt.feature.home.generated.resources.feature_home_dashboard_offline_body
import kpt.feature.home.generated.resources.feature_home_dashboard_retry
import kpt.feature.home.generated.resources.feature_home_dashboard_retry_sync
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * What went wrong, in two parts — 03's "**Synchronization Timeout:** Network synchronization timed out
 * for this section": a bold kind (with the HTTP status when there is one), then the server's own
 * sentence. Fineract's `defaultUserMessage` comes through `ForkErrorMapper`; without a Fineract
 * body the categorised copy shows — never the raw transport message (that is the whole request URL
 * plus the JSON body).
 */
@Composable
internal fun errorKind(error: Throwable?, offline: Boolean): String {
    if (offline || error == null) {
        return stringResource(
            if (offline) {
                Res.string.feature_home_dashboard_error_kind_offline
            } else {
                Res.string.feature_home_dashboard_error_kind_failed
            },
        )
    }
    return categorize(error).kind()
}

/** The bold kind for a categorised failure, with the HTTP status when the category carries one. */
@Composable
private fun ErrorCategory.kind(): String {
    val cat = this
    return when (cat) {
        ErrorCategory.Network -> stringResource(Res.string.feature_home_dashboard_error_kind_offline)
        ErrorCategory.Timeout.Connect, ErrorCategory.Timeout.Read -> stringResource(
            Res.string.feature_home_dashboard_error_kind_timeout,
        )
        ErrorCategory.Auth -> "${stringResource(
            Res.string.feature_home_dashboard_error_kind_signed_out,
        )} (${HTTP_UNAUTHORIZED})"
        ErrorCategory.RateLimit, ErrorCategory.QuotaExceeded -> stringResource(
            Res.string.feature_home_dashboard_error_kind_busy,
        )
        is ErrorCategory.ClientError -> {
            val kind = when (cat.httpCode) {
                HTTP_FORBIDDEN -> Res.string.feature_home_dashboard_error_kind_forbidden
                HTTP_NOT_FOUND -> Res.string.feature_home_dashboard_error_kind_not_found
                else -> Res.string.feature_home_dashboard_error_kind_rejected
            }
            "${stringResource(kind)} (${cat.httpCode})"
        }
        is ErrorCategory.Server -> stringResource(Res.string.feature_home_dashboard_error_kind_server) + (
            cat.httpCode?.let {
                " ($it)"
            } ?: ""
            )
        ErrorCategory.Generic -> stringResource(Res.string.feature_home_dashboard_error_kind_failed)
    }
}

/**
 * 03 `#card_error_retry`: rounded-xl red-200 border, red-50/80 fill, p-3.5; error glyph, cached time,
 * the server's reason, red Retry.
 */
@Composable
internal fun CardError(
    onRetry: () -> Unit,
    error: Throwable? = null,
    offline: Boolean = false,
    cachedAt: Instant? = null,
) {
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
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = f.moneyNegative,
                modifier = Modifier.padding(top = 0.5f.tw).size(5.tw),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.tw)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.feature_home_dashboard_card_error),
                        modifier = Modifier.weight(1f),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = f.moneyNegative,
                    )
                    cachedAt?.let {
                        val t = it.toLocalDateTime(TimeZone.currentSystemDefault()).time
                        val hm = "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}"
                        Text(
                            stringResource(Res.string.feature_home_dashboard_card_cached, hm.clock12()),
                            style = KptTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = c.onSurfaceVariant,
                        )
                    }
                }
                // text-[12px] slate-700 leading-relaxed ·
                // <strong class="font-medium text-slate-900">Kind:</strong> reason
                val kind = errorKind(error, offline)
                val reason = error?.let {
                    rememberAppErrorMessageFor()(it)
                } ?: stringResource(Res.string.feature_home_dashboard_card_error_body)
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
                    Icon(
                        Icons.Outlined.SyncProblem,
                        contentDescription = null,
                        tint = c.surfaceContainerLowest,
                        modifier = Modifier.size(3.75f.tw),
                    )
                    Text(
                        stringResource(Res.string.feature_home_dashboard_retry),
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.surfaceContainerLowest,
                    )
                }
            }
        }
    }
}

/** 04 `#screen_error_retry`: white rounded-xl card, h-1 red top bar, w-12 sync-problem badge, full-width Retry. */
@Composable
internal fun ScreenError(onRetryAll: () -> Unit) {
    val f = MaterialTheme.finance
    val c = KptTheme.colorScheme
    val edge = f.moneyNegative.copy(alpha = EDGE_ALPHA)
    Surface(
        Modifier.fillMaxWidth().testTag(TestTags.Home.SCREEN_ERROR),
        shape = KptTheme.shapes.medium,
        border = BorderStroke(HAIRLINE, edge),
        elevation = CARD_SHADOW,
    ) {
        Box {
            Column(Modifier.fillMaxWidth().padding(5.tw), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(
                        12.tw,
                    ).background(f.moneyNegativeContainer, CircleShape).border(HAIRLINE, edge, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.SyncProblem,
                        contentDescription = null,
                        tint = f.moneyNegative,
                        modifier = Modifier.size(6.5f.tw),
                    )
                }
                Spacer(Modifier.height(3.tw))
                Text(
                    stringResource(Res.string.feature_home_dashboard_error_title),
                    style = KptTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = TIGHT,
                    color = c.onSurface,
                )
                Text(
                    stringResource(Res.string.feature_home_dashboard_error_body),
                    modifier = Modifier.padding(top = 1.5f.tw),
                    style = KptTheme.typography.labelMedium,
                    fontWeight = FontWeight.Normal,
                    color = c.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                PrimaryButton(
                    stringResource(Res.string.feature_home_dashboard_retry),
                    AppIcons.Sync,
                    KptTheme.shapes.small,
                    Modifier.padding(top = 4.tw),
                    onRetryAll,
                )
            }
            Box(Modifier.fillMaxWidth().height(1.tw).background(f.moneyNegative))
        }
    }
}

/** 05: white rounded-2xl p-6 card, w-16 rounded-2xl wifi-off badge, text-lg title, rounded-xl "Retry sync". */
@Composable
internal fun OfflineError(onRetryAll: () -> Unit) {
    val f = MaterialTheme.finance
    val c = KptTheme.colorScheme
    val edge = f.moneyNegative.copy(alpha = SOFT_EDGE_ALPHA)
    Surface(
        Modifier.fillMaxWidth().testTag(TestTags.Home.SCREEN_ERROR),
        shape = KptTheme.shapes.large,
        border = BorderStroke(HAIRLINE, edge),
        elevation = ELEVATED_SHADOW,
    ) {
        Column(Modifier.fillMaxWidth().padding(6.tw), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(
                    16.tw,
                ).shadow(
                    CARD_SHADOW,
                    KptTheme.shapes.large,
                ).background(
                    f.moneyNegativeContainer,
                    KptTheme.shapes.large,
                ).border(HAIRLINE, edge, KptTheme.shapes.large),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcons.WifiOff,
                    contentDescription = null,
                    tint = f.moneyNegative,
                    modifier = Modifier.size(7.5f.tw),
                )
            }
            Spacer(Modifier.height(4.tw))
            Text(
                stringResource(Res.string.feature_home_dashboard_error_title),
                style = KptTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = TIGHT,
                color = c.onSurface,
            )
            Text(
                stringResource(Res.string.feature_home_dashboard_offline_body),
                modifier = Modifier.padding(top = 1.5f.tw),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Normal,
                color = c.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                stringResource(Res.string.feature_home_dashboard_retry_sync),
                AppIcons.Refresh,
                KptTheme.shapes.medium,
                Modifier.padding(top = 5.tw),
                onRetryAll,
            )
        }
    }
}

/** 05 resilience banner: rounded-xl p-3.5 emerald-50/80, emerald-200 border, cloud-sync glyph. */
@Composable
internal fun OfflineBanner() {
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
        Icon(
            Icons.Outlined.CloudSync,
            contentDescription = null,
            tint = c.secondary,
            modifier = Modifier.padding(top = 0.5f.tw).size(5.tw),
        )
        Column {
            Text(
                stringResource(Res.string.feature_home_dashboard_offline_banner_title),
                style = KptTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = c.onSecondaryContainer,
            )
            Text(
                stringResource(Res.string.feature_home_dashboard_offline_banner_body),
                modifier = Modifier.padding(top = 0.5f.tw),
                style = KptTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
                color = c.secondary,
            )
        }
    }
}

/** Full-width min-h-[48px] primary button, text-sm semibold with a leading glyph. */
@Composable
internal fun PrimaryButton(
    text: String,
    icon: ImageVector,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
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
