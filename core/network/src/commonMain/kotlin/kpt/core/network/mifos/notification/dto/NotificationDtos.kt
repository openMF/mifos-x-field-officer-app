/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.notification.dto

import kotlinx.serialization.Serializable

/** `GET notifications` — one page; Fineract returns only unread rows unless `isRead=true`. */
@Serializable
data class NotificationPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<NotificationDto> = emptyList())

/** A Fineract notification: [objectType] / [objectId] name the record it is about (`loan` 1439). */
@Serializable
data class NotificationDto(
    val id: Long = 0,
    val objectType: String? = null,
    val objectId: Long? = null,
    val action: String? = null,
    val actorId: Long? = null,
    val content: String? = null,
    val isRead: Boolean = false,
    val isSystemGenerated: Boolean = false,
    val createdAt: String? = null,
)
