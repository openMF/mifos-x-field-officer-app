/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.recurringdeposit

import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.recurringdeposit.dao.RecurringDepositDao
import kpt.core.database.recurringdeposit.entity.RecurringDepositTemplateEntity
import kpt.core.database.utils.getCurrentTimeInMillis
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate
import kpt.core.network.mifos.recurringdeposit.api.RecurringAccountApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Stored as a serialized payload rather than shredded into columns: the template is a deep
 * read-only projection (product options, charts, chargeable options) that is only ever handed to
 * the account-creation form whole, so columns would buy no query power and would need migrating
 * every time Fineract adds an option field.
 */
private val templateJson = Json { ignoreUnknownKeys = true }

@StoreProvider(id = "recurringDepositTemplate", ttl = "12h")
@CacheKey(
    fn = "forClient",
    key = "recurringDepositTemplate:{clientId}",
    params = ["clientId:Int"],
)
fun provideRecurringDepositTemplateStore(
    service: RecurringAccountApi,
    dao: RecurringDepositDao,
): Store<Int, RecurringDepositAccountTemplate> = StoreFactory.createStore(
    fetcher = Fetcher.of { clientId: Int ->
        service.getRecurringDepositAccountTemplate(clientId = clientId, productId = null)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { clientId: Int ->
            dao.observeTemplate(clientId).map { row ->
                row?.let { templateJson.decodeFromString<RecurringDepositAccountTemplate>(it.payload) }
            }
        },
        writer = { clientId: Int, template: RecurringDepositAccountTemplate ->
            dao.upsert(
                RecurringDepositTemplateEntity(
                    clientId = clientId,
                    payload = templateJson.encodeToString(template),
                    fetchedAtMs = getCurrentTimeInMillis(),
                ),
            )
        },
    ),
)
