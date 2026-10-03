/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.survey

import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.survey.dao.SurveyDao
import kpt.core.database.survey.entity.SurveyEntity
import kpt.core.network.mifos.survey.api.SurveyApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Surveys — READ-CACHE (`createStore`).
 *
 * Survey definitions are server-authoritative reference data that a field officer must be able to
 * OPEN offline, since surveys are administered in the field where connectivity is the exception.
 * Caching the definitions is what makes that possible; the RESPONSES are a write path and are not
 * part of this store (D1 keeps submissions online-only / on the `feature:offline` queue).
 */
@StoreProvider(id = "surveys", ttl = "24h")
@CacheKey(name = "LIST", key = "surveys")
fun provideSurveyStore(
    service: SurveyApi,
    dao: SurveyDao,
): Store<Unit, List<SurveyEntity>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.allSurveys() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllSurveys() },
        writer = { _: Unit, surveys: List<SurveyEntity> -> surveys.forEach { dao.insertSurvey(it) } },
    ),
)
