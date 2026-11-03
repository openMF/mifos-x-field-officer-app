/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.notification.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.notification.AppNotification
import org.jetbrains.compose.ui.tooling.preview.Preview
import kpt.core.base.designsystem.theme.KptTheme as KptBaseTheme

/** The gateway's own rows (2026-10-27): repayments on loans, plus one unread client approval. */
private val sample = listOf(
    AppNotification(
        3001,
        "client",
        192,
        "approved",
        "Client approved",
        isRead = false,
        createdAt = "2026-10-27T09:10:00",
    ),
    AppNotification(
        3000,
        "loan",
        1439,
        "repaymentMade",
        "Repayment made",
        isRead = false,
        createdAt = "2026-10-27T08:40:00",
    ),
    AppNotification(
        2950,
        "loan",
        1439,
        "repaymentMade",
        "Repayment made",
        isRead = true,
        createdAt = "2026-10-21T20:25:31.173940",
    ),
    AppNotification(
        2948,
        "loan",
        300,
        "repaymentMade",
        "Repayment made",
        isRead = true,
        createdAt = "2026-10-21T19:02:11",
    ),
    AppNotification(
        2940,
        "savingsAccount",
        88,
        "deposit",
        "Deposit made",
        isRead = true,
        createdAt = "2026-10-20T11:00:00",
    ),
)

@Composable
private fun Render(state: ScreenState<List<AppNotification>>) = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.background) {
        NotificationsScreenContent(notifications = state, marking = false, onAction = {})
    }
}

@Preview @Composable
internal fun NotificationsListPreview() = Render(ScreenState.Content(sample))

@Preview @Composable
internal fun NotificationsEmptyPreview() = Render(ScreenState.Content(emptyList()))

@Preview @Composable
internal fun NotificationsLoadingPreview() = Render(ScreenState.Loading)

@Preview @Composable
internal fun NotificationsErrorPreview() = Render(ScreenState.Error(IllegalStateException("preview")))
