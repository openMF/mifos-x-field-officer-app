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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `QuestionDatasEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class QuestionDatas(
    /** The server id of this record. */
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
    val responseDatas: List<ResponseDatas> = emptyList(),
) {

    /** The server id of the question. */
    var questionId: Int
        get() = id
        set(id) {
            this.id = id
        }
}
