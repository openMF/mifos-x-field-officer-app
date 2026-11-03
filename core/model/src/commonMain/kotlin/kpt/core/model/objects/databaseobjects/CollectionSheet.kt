/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A collection sheet. */
@Serializable
data class CollectionSheet(
    /** The date the amount is due. */
    var dueDate: IntArray,
    /** The groups. */
    var groups: List<AppGroup>,
) {
    override fun toString(): String {
        return Json.encodeToString(serializer(), this)
    }
}
