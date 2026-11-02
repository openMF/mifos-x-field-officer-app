/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.checkerinbox

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask

interface CheckerInboxRepository {
    /** Maker-checker tasks awaiting this user's approval. */
    fun checkerTasksStream(scope: CoroutineScope): ScreenDataStream<List<CheckerTask>>

    /** Loan reschedule requests awaiting approval. Deciding one is the loan's own reschedule screen. */
    fun rescheduleRequestsStream(scope: CoroutineScope): ScreenDataStream<List<RescheduleLoansTask>>

    /** The action and entity names the inbox can be filtered by; null when they could not be read. */
    suspend fun searchTemplate(): CheckerInboxSearchTemplate?
}
