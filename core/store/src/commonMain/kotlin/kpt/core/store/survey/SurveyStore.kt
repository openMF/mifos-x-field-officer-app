/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.survey

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.survey.dao.SurveyDao
import kpt.core.model.survey.Survey
import kpt.core.network.mifos.survey.api.SurveyApi
import kpt.core.store.mapping.toEntity
import kpt.core.store.mapping.toModel
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
): Store<Unit, List<Survey>> = StoreFactory.createStore(
    fetcher = Fetcher.of { _: Unit -> service.allSurveys() },
    sourceOfTruth = SourceOfTruth.of(
        reader = { _: Unit -> dao.getAllSurveys().map { rows -> rows.map { it.toModel() } } },
        // REPLACE: the fetch is the server's whole list, so a survey it no longer has must go.
        writer = { _: Unit, surveys: List<Survey> -> dao.replaceAllSurveys(surveys.map { it.toEntity() }) },
        delete = { _: Unit -> dao.deleteAllSurveys() },
        // What the logout purge (`Store.clear()`) calls; without it the rows outlived the session.
        deleteAll = { dao.deleteAllSurveys() },
    ),
)
