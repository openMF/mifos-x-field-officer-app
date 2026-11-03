/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.mapping

import kpt.core.model.survey.ComponentDatas
import kpt.core.model.survey.QuestionDatas
import kpt.core.model.survey.ResponseDatas
import kpt.core.model.survey.Survey
import kpt.core.database.survey.entity.ComponentDatasEntity as ComponentDatasRow
import kpt.core.database.survey.entity.QuestionDatasEntity as QuestionDatasRow
import kpt.core.database.survey.entity.ResponseDatasEntity as ResponseDatasRow
import kpt.core.database.survey.entity.SurveyEntity as SurveyRow

/** Maps the stored row to its `Survey` model. */
fun SurveyRow.toModel(): Survey = Survey(
    id = id,
    key = key,
    name = name,
    description = description,
    isSync = isSync,
    countryCode = countryCode,
    questionDatas = questionDatas.map { it.toModel() },
    componentDatas = componentDatas.map { it.toModel() },
)

/** Maps the model to its stored `SurveyRow`. */
fun Survey.toEntity(): SurveyRow = SurveyRow(
    id = id,
    key = key,
    name = name,
    description = description,
    isSync = isSync,
    countryCode = countryCode,
    questionDatas = questionDatas.map { it.toEntity() },
    componentDatas = componentDatas.map { it.toEntity() },
)

/** Maps the stored row to its `ComponentDatas` model. */
fun ComponentDatasRow.toModel(): ComponentDatas = ComponentDatas(
    id = id,
    key = key,
    text = text,
    description = description,
    sequenceNo = sequenceNo,
)

/** Maps the model to its stored `ComponentDatasRow`. */
fun ComponentDatas.toEntity(): ComponentDatasRow = ComponentDatasRow(
    id = id,
    key = key,
    text = text,
    description = description,
    sequenceNo = sequenceNo,
)

/** Maps the stored row to its `QuestionDatas` model. */
fun QuestionDatasRow.toModel(): QuestionDatas = QuestionDatas(
    id = id,
    surveyId = surveyId,
    componentKey = componentKey,
    key = key,
    text = text,
    description = description,
    sequenceNo = sequenceNo,
    responseDatas = responseDatas.map { it.toModel() },
)

/** Maps the model to its stored `QuestionDatasRow`. */
fun QuestionDatas.toEntity(): QuestionDatasRow = QuestionDatasRow(
    id = id,
    surveyId = surveyId,
    componentKey = componentKey,
    key = key,
    text = text,
    description = description,
    sequenceNo = sequenceNo,
    responseDatas = responseDatas.map { it.toEntity() },
)

/** Maps the stored row to its `ResponseDatas` model. */
fun ResponseDatasRow.toModel(): ResponseDatas = ResponseDatas(
    responseId = responseId,
    id = id,
    text = text,
    sequenceNo = sequenceNo,
    value = value,
)

/** Maps the model to its stored `ResponseDatasRow`. */
fun ResponseDatas.toEntity(): ResponseDatasRow = ResponseDatasRow(
    responseId = responseId,
    id = id,
    text = text,
    sequenceNo = sequenceNo,
    value = value,
)
