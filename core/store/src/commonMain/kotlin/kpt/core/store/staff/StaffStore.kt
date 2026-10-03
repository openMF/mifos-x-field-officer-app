/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.staff

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.staff.dao.StaffDao
import kpt.core.database.staff.entity.StaffEntity
import kpt.core.network.mifos.staff.api.StaffApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Staff for an office — READ-CACHE (`createStore`), keyed by office.
 *
 * The key is the officeId because both sides are office-scoped: `getStaffForOffice(officeId)` on
 * the wire, `getAllStaff(officeId)` in Room. Keying by office is what keeps two offices' rosters in
 * separate cache slots — a bare `Unit` key would let the second office's fetch overwrite the first
 * (the Store5 `inconsistent-read-paths` anti-pattern).
 */
@StoreProvider(id = "staff", ttl = "24h")
@CacheKey(fn = "forOffice", key = "staff:{officeId}", params = ["officeId:Int"])
fun provideStaffStore(
    service: StaffApi,
    dao: StaffDao,
): Store<Int, List<StaffEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { officeId: Int -> service.getStaffForOffice(officeId) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { officeId: Int -> dao.getAllStaff(officeId) },
        writer = { _: Int, staff: List<StaffEntity> -> dao.insertStaffs(staff) },
    ),
)
