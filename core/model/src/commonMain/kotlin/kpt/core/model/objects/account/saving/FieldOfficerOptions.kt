/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.saving

import kotlinx.serialization.Serializable

/**
 * Created by nkiboi on 12/15/2015.
 */
@Serializable
class FieldOfficerOptions(
    /** The server id of this record. */
    var id: Int? = null,

    /** The person's first name. */
    var firstname: String? = null,

    /** The person's last name. */
    var lastname: String? = null,

    /** The display name. */
    var displayName: String? = null,
)
