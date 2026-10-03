/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.responses

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kpt.core.model.objects.Changes

@Serializable
class SaveResponse(
    var groupId: Int? = null,

    var resourceId: Int? = null,

    var officeId: Int? = null,
    var changes: Changes? = null,
) {
    override fun toString(): String {
        return Json.encodeToString(serializer(), this)
    }
}
