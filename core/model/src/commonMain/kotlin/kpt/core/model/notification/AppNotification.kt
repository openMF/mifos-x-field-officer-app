/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.notification

import kotlinx.serialization.Serializable

/**
 * One Fineract notification. [objectType] (`loan`, `client`, `group`, `center`, `savingsAccount` …)
 * and [objectId] name the record it is about; [createdAt] is the server's ISO local date-time.
 */
@Serializable
data class AppNotification(
    /** The server id of this record. */
    val id: Long,
    /** The kind of record the notification is about (`loan`, `client`, …). */
    val objectType: String,
    /** The id of that record, when there is one. */
    val objectId: Long?,
    /** The action. */
    val action: String,
    /** The notification text. */
    val content: String,
    /** True once the notification has been read. */
    val isRead: Boolean,
    /** When it was created, as the server's ISO local date-time. */
    val createdAt: String,
)
