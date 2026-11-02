/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recent

import kotlinx.serialization.Serializable

/** What a recently-opened record is — decides its icon and which profile reopens it. */
@Serializable
enum class RecentKind { CLIENT, GROUP, CENTER, LOAN }

/**
 * A record the officer opened, for Home's "Recently opened". [detail] is the secondary line the
 * record carries itself (a loan's client name); the kind label and relative time are rendered.
 */
@Serializable
data class RecentRecord(
    val kind: RecentKind,
    val id: Long,
    val title: String,
    val accountNo: String? = null,
    val detail: String? = null,
    val openedAtMs: Long = 0,
)
