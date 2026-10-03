/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.survey.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "QuestionDatas",
)
data class QuestionDatasEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    val surveyId: Int = 0,

    val componentKey: String? = null,

    val key: String? = null,

    val text: String? = null,

    val description: String? = null,

    val sequenceNo: Int = 0,

    val responseDatas: List<ResponseDatasEntity> = emptyList(),
) {

    var questionId: Int
        get() = id
        set(id) {
            this.id = id
        }
}
