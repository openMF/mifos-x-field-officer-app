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

/**
 * `GET notifications` — one page; Fineract returns only unread rows unless `isRead=true`.
 *
 * @property pageItems the records on this page
 * @property totalFilteredRecords the number of records matching the query, across every page
 */
@Serializable
data class NotificationPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<NotificationDto> = emptyList())

/** A Fineract notification: [objectType] / [objectId] name the record it is about (`loan` 1439). */
@Serializable
data class NotificationDto(
    /** The server id of this record. */
    val id: Long = 0,
    /** The kind of record the notification is about. */
    val objectType: String? = null,
    /** The id of that record, when there is one. */
    val objectId: Long? = null,
    /** The action. */
    val action: String? = null,
    /** The server id of the user whose action raised the notification. */
    val actorId: Long? = null,
    /** The notification text. */
    val content: String? = null,
    /** True once the notification has been read. */
    val isRead: Boolean = false,
    /** True when the system raised the notification. */
    val isSystemGenerated: Boolean = false,
    /** When the notification was created. */
    val createdAt: String? = null,
)
