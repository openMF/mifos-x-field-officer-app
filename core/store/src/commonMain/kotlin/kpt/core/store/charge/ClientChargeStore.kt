/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.charge

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.charge.dao.ChargeDao
import kpt.core.database.charge.entity.ChargesEntity
import kpt.core.network.mifos.charge.api.ChargeApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Client charges — READ-CACHE (`createStore`), keyed by clientId.
 *
 * Keyed because both sides are client-scoped (`getListOfClientCharges(resourceType, resourceId)`
 * on the wire, `getClientCharges(clientId)` in Room). A `Unit` key would collapse every client's
 * charges into one slot and serve the previously-viewed client's rows — the Store5
 * `inconsistent-read-paths` anti-pattern.
 *
 * `resourceType` is pinned to "clients": the endpoint is shared with loans/savings, but this store
 * is the CLIENT charge read. A loan-charge store would be its own provider with its own key.
 */
@StoreProvider(id = "clientCharges", ttl = "15m")
@CacheKey(fn = "forClient", key = "clientCharges:{clientId}", params = ["clientId:Int"])
fun provideClientChargeStore(
    service: ChargeApi,
    dao: ChargeDao,
): Store<Int, List<ChargesEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { clientId: Int ->
        service.getListOfClientCharges(resourceType = CLIENT_RESOURCE, resourceId = clientId).pageItems
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { clientId: Int -> dao.getClientCharges(clientId) },
        writer = { _: Int, charges: List<ChargesEntity> -> dao.insertAllCharges(charges) },
    ),
)

private const val CLIENT_RESOURCE = "clients"
