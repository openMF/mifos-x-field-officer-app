/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.note

import kotlinx.serialization.Serializable

/** Server response to a create note request. */
@Serializable
data class CreateNoteResponseDto(
    /** The server id of the office. */
    val officeId: Long? = null,
    /** The server id of the client. */
    val clientId: Long? = null,
    /** The server id of the resource the request created or changed. */
    val resourceId: Long? = null,
)
