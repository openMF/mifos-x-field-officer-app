/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.client

import kotlinx.serialization.Serializable

/**
 * Created by mayankjindal on 13/12/16.
 */
@Serializable
data class IncomeOrLiabilityAccount(
    val id: Int,
    val name: String,
    val glCode: String,
)
