/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.responses

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kpt.core.model.objects.Changes

/** Server response to a save request. */
@Serializable
class SaveResponse(
    /** The server id of the group. */
    var groupId: Int? = null,

    /** The server id of the resource the request created or changed. */
    var resourceId: Int? = null,

    /** The server id of the office. */
    var officeId: Int? = null,
    /** The fields the request changed, as Fineract reports them. */
    var changes: Changes? = null,
) {
    override fun toString(): String {
        return Json.encodeToString(serializer(), this)
    }
}
