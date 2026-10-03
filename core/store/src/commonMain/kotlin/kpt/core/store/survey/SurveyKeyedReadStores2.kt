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
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.survey.entity.SurveyEntity
import kpt.core.network.mifos.survey.api.SurveyApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Keyed reads for survey with no table of their own — cached through the shared read cache so the
 * last-known answer still renders with no signal.
 */

@StoreProvider(id = "surveyGetSurvey", ttl = "12h")
@CacheKey(fn = "forKey", key = "surveyGetSurvey:{key}", params = ["key:String"])
fun provideGetSurveyStore(
    surveyApi: SurveyApi,
    cache: ApiResponseCacheDao,
): Store<Int, SurveyEntity> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.SurveyGetSurvey.forKey(key.toString()) },
    fetch = { key -> surveyApi.getSurvey(surveyId = key) },
)
