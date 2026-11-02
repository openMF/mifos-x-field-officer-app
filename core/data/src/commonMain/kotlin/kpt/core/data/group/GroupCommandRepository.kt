/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.group

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.group.GroupPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.responses.SaveResponse

/**
 * Every group write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * They run online-only, and a write the server accepts is recorded locally through the group
 * stores, so the reads see it. Offline CAPTURE is a different operation with its own explicit queue
 * ([kpt.core.data.sync.OfflineQueueRepository]); it is never a silent fallback from here.
 */
interface GroupCommandRepository {

    suspend fun createGroup(groupPayload: GroupPayload?): MutationResult<SaveResponse>

    suspend fun activateGroup(groupId: Int, activatePayload: ActivatePayload): MutationResult<Unit>
}
