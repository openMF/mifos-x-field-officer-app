/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.checkerinboxtask

import kotlinx.serialization.Serializable
/** A reschedule reason code value. */
@Serializable
data class RescheduleReasonCodeValue(
    /** The server id of this record. */
    var id: Int = 0,
    /** The display name. */
    var name: String = "",
    /** True when the record is active. */
    var active: Boolean = true,
    /** True when this option must be filled in. */
    var mandatory: Boolean = false,
)
