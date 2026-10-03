/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 17-07-2017.
 */
@Serializable
class LoanAndClientName(
    val loan: LoanCollectionSheet?,
    val clientName: String?,
    val id: Int,
)
