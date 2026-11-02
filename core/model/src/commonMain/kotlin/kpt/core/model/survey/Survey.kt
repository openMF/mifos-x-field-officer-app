/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.survey

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SurveyEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Survey(
    val id: Int = 0,
    val key: String? = null,
    val name: String? = null,
    val description: String? = null,
    val isSync: Boolean = false,
    val countryCode: String? = null,
    val questionDatas: List<QuestionDatas> = emptyList(),
    val componentDatas: List<ComponentDatas> = emptyList(),
)
