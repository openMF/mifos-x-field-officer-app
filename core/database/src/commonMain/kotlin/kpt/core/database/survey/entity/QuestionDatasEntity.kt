/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.survey.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
/** Room row for a survey question. */
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
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    /** The server id of the survey. */
    val surveyId: Int = 0,

    /** The key of the component the question belongs to. */
    val componentKey: String? = null,

    /** The key of this survey item. */
    val key: String? = null,

    /** The text shown for this item. */
    val text: String? = null,

    /** A free-text description. */
    val description: String? = null,

    /** The order this item is shown in. */
    val sequenceNo: Int = 0,

    /** The question's possible answers. */
    val responseDatas: List<ResponseDatasEntity> = emptyList(),
) {

    /** The server id of the question. */
    var questionId: Int
        get() = id
        set(id) {
            this.id = id
        }
}
