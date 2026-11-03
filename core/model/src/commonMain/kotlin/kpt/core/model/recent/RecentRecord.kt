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

/**
 * What a recently-opened record is — decides its icon and which profile reopens it.
 *
 * @property CENTER A centre
 * @property CLIENT A client
 * @property GROUP A group
 * @property LOAN A loan
 * @property CENTER A centre
 * @property CLIENT A client
 * @property GROUP A group
 * @property LOAN A loan
 */
@Serializable
enum class RecentKind {
    /** A client. */
    CLIENT,

    /** A group. */
    GROUP,

    /** A centre. */
    CENTER,

    /** A loan account. */
    LOAN,
}

/**
 * A record the officer opened, for Home's "Recently opened". [detail] is the secondary line the
 * record carries itself (a loan's client name); the kind label and relative time are rendered.
 */
@Serializable
data class RecentRecord(
    /** The kind of record. */
    val kind: RecentKind,
    /** The server id of this record. */
    val id: Long,
    /** The title. */
    val title: String,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The secondary line the record carries, such as a loan's client name. */
    val detail: String? = null,
    /** When the record was last opened, in epoch milliseconds. */
    val openedAtMs: Long = 0,
)
