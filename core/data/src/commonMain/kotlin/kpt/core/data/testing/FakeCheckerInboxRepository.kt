/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.checkerinbox.CheckerInboxRepository
import kpt.core.model.objects.checkerinboxtask.CheckerInboxSearchTemplate
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.objects.checkerinboxtask.RescheduleLoansTask

/**
 * Scripted inbox. [tasks] and [reschedules] stream; [FakeCheckerInboxCommandRepository] removes decided
 * tasks from [tasks].
 */
@ExperimentalDataTestingApi
class FakeCheckerInboxRepository(
    tasks: List<CheckerTask> = emptyList(),
    reschedules: List<RescheduleLoansTask> = emptyList(),
    private val template: CheckerInboxSearchTemplate? = CheckerInboxSearchTemplate(),
) : CheckerInboxRepository {

    val tasks = MutableStateFlow<ScreenState<List<CheckerTask>>>(ScreenState.Content(tasks))
    val reschedules = MutableStateFlow<ScreenState<List<RescheduleLoansTask>>>(ScreenState.Content(reschedules))

    override fun checkerTasksStream(scope: CoroutineScope) = screenDataStreamForTesting(tasks)

    override fun rescheduleRequestsStream(scope: CoroutineScope) = screenDataStreamForTesting(reschedules)

    override suspend fun searchTemplate() = template

    /** What the real store does when the server accepts a decision: the task leaves the queue. */
    fun remove(auditId: Int) {
        val current = (tasks.value as? ScreenState.Content)?.data ?: return
        tasks.value = ScreenState.Content(current.filterNot { it.id == auditId })
    }
}
