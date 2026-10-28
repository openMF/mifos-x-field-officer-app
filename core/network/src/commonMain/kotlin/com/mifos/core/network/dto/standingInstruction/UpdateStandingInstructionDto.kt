/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.network.dto.standingInstruction

import kotlinx.serialization.Serializable

@Serializable
data class UpdateStandingInstructionDto(
    val amount: String? = null,
    val dateFormat: String? = null,
    val instructionType: String? = null,
    val locale: String? = null,
    val monthDayFormat: String? = null,
    val name: String? = null,
    val priority: String? = null,
    val recurrenceFrequency: String? = null,
    val recurrenceInterval: String? = null,
    val recurrenceOnMonthDay: String? = null,
    val recurrenceType: String? = null,
    val status: String? = null,
    val validFrom: String? = null,
    val validTill: String? = null,
)
