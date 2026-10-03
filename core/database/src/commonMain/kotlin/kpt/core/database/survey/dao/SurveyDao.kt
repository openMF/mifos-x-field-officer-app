/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.survey.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.survey.entity.QuestionDatasEntity
import kpt.core.database.survey.entity.ResponseDatasEntity
import kpt.core.database.survey.entity.SurveyEntity

/**
 * Created by Pronay Sarker on 12/02/2025 (9:33 PM)
 */
@DbDao
@Dao
interface SurveyDao {

    @Insert(entity = SurveyEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Insert(entity = QuestionDatasEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionData(questionData: QuestionDatasEntity)

    @Insert(entity = ResponseDatasEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponseData(responseDatas: ResponseDatasEntity)

    @Query("SELECT * FROM Survey")
    fun getAllSurveys(): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM QuestionDatas WHERE surveyId = :surveyId ORDER BY sequenceNo ASC")
    fun getQuestionDatas(surveyId: Int): Flow<List<QuestionDatasEntity>>

    @Query("SELECT * FROM ResponseDatas WHERE id = :questionId ORDER BY sequenceNo ASC")
    fun getResponseDatas(questionId: Int): Flow<List<ResponseDatasEntity>>

    /** Stamp the owning survey onto question data before persisting. Was `SurveyDaoHelper`. */
    suspend fun saveQuestionDataFor(surveyId: Int, questionData: QuestionDatasEntity): QuestionDatasEntity {
        val stamped = questionData.copy(surveyId = surveyId)
        insertQuestionData(stamped)
        return stamped
    }

    /** Stamp the owning question onto response data before persisting. Was `SurveyDaoHelper`. */
    suspend fun saveResponseDataFor(questionId: Int, responseData: ResponseDatasEntity): ResponseDatasEntity {
        val stamped = responseData.copy(id = questionId)
        insertResponseData(stamped)
        return stamped
    }
}
