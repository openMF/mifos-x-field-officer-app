/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.notification.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Query
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.network.mifos.notification.dto.NotificationPageDto

/** Fineract's in-app notifications — what the top bar's bell opens. No original: the app being ported had none. */
@ApiBinding("mifos")
interface NotificationApi {

    /** Unread only by default; pass [isRead] `true` for the read history. Newest first. */
    @GET("notifications")
    suspend fun getNotifications(
        @Query("isRead") isRead: Boolean,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int,
        @Query("orderBy") orderBy: String = "id",
        @Query("sortOrder") sortOrder: String = "DESC",
    ): NotificationPageDto

    /** Marks every notification of the signed-in user read. Fineract answers with an empty body. */
    @PUT("notifications")
    suspend fun markAllRead()
}
