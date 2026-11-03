/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.recurringdeposit

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.recurringdeposit.RecurringDepositApplicationInput

/**
 * Every recurringdeposit write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * Online-required commands: once the server accepts one, the implementation records the outcome
 * through the stores the reads observe, so no caller refreshes anything. Offline CAPTURE is a
 * different operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it
 * is never a silent fallback from here.
 */
interface RecurringDepositCommandRepository {

    /**
     * Submit a new recurring-deposit application.
     *
     * Takes a `core/model` input and returns `MutationResult<Unit>`: a feature module must not
     * depend on `core:network`, and no caller needs the raw response.
     */
    suspend fun createRecurringDepositAccount(
        input: RecurringDepositApplicationInput,
    ): MutationResult<Unit>
}
