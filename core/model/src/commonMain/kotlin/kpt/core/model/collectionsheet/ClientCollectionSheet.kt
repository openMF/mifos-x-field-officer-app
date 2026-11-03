/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet

import kotlinx.serialization.Serializable
import kpt.core.model.objects.collectionsheets.AttendanceTypeOption
import kpt.core.model.objects.collectionsheets.LoanCollectionSheet

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientCollectionSheet`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ClientCollectionSheet(
    /** The server id of the client. */
    var clientId: Int = 0,
    /** The client's display name. */
    var clientName: String? = null,
    /** The loans. */
    var loans: ArrayList<LoanCollectionSheet>? = null,
    /** Whether the client attended the meeting. */
    var attendanceType: AttendanceTypeOption? = null,
    /** The savings accounts. */
    var savings: ArrayList<SavingsCollectionSheet> = ArrayList(),
)
