/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.survey.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.survey.entity.ComponentDatasEntity
import kpt.core.database.survey.entity.QuestionDatasEntity
import kpt.core.database.survey.entity.ResponseDatasEntity

/**
 * Created by Pronay Sarker on 13/02/2025 (2:53 AM)
 */
@DbConverters
class SurveyTypeConverters {

    /** Stores a `List<QuestionDatasEntity>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromQuestionDatasList(questionDatas: List<QuestionDatasEntity>?): String? {
        return questionDatas?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<QuestionDatasEntity>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toQuestionDatasList(json: String?): List<QuestionDatasEntity>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<ComponentDatasEntity>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromComponentDatasList(componentDatas: List<ComponentDatasEntity>?): String? {
        return componentDatas?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<ComponentDatasEntity>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toComponentDatasList(json: String?): List<ComponentDatasEntity>? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `List<ResponseDatasEntity>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromResponseDatasList(responseDatas: List<ResponseDatasEntity>?): String? {
        return responseDatas?.let { Json.encodeToString(it) }
    }

    /** Reads a `List<ResponseDatasEntity>?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toResponseDatasList(json: String?): List<ResponseDatasEntity>? {
        return json?.let { Json.decodeFromString(it) }
    }
}
