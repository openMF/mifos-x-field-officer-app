/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.office

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.office.entity.OfficeEntity

interface OfficeRepository {
    /** Every office in the tenant. Reference data — the officer's whole hierarchy picker depends on it. */
    fun officesStream(scope: CoroutineScope): ScreenDataStream<List<OfficeEntity>>
}
