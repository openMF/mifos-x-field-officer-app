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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate

interface RecurringDepositRepository {
    /** The account-creation template for one client. */
    fun templateStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<RecurringDepositAccountTemplate>
}
