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
    val id: Long,
    val objectType: String,
    val objectId: Long?,
    val action: String,
    val content: String,
    val isRead: Boolean,
    val createdAt: String,
)
