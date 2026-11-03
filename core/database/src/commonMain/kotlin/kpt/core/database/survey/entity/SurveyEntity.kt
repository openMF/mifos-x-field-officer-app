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
/** Room row for a survey. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "Survey",
)
data class SurveyEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The key of this survey item. */
    val key: String? = null,

    /** The display name. */
    val name: String? = null,

    /** A free-text description. */
    val description: String? = null,

    /** True when the survey has been synced. */
    val isSync: Boolean = false,

    /** The country code of the survey. */
    val countryCode: String? = null,

    /** The survey's questions. */
    val questionDatas: List<QuestionDatasEntity> = emptyList(),

    /** The survey's components. */
    val componentDatas: List<ComponentDatasEntity> = emptyList(),
)
