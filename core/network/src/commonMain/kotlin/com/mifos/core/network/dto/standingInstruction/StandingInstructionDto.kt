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
data class StandingInstructionDto(
    val id: Int,
    val accountDetailId: Int? = null,
    val name: String? = null,
    val amount: Double? = null,
    val validFrom: String? = null,
    val recurrenceInterval: Int? = null,
    val recurrenceOnMonthDay: String? = null,

    val instructionType: CodeDescriptionDto? = null,
    val priority: CodeDescriptionDto? = null,
    val recurrenceFrequency: CodeDescriptionDto? = null,
    val recurrenceType: CodeDescriptionDto? = null,
    val status: CodeDescriptionDto? = null,
    val transferType: CodeDescriptionDto? = null,

    // From
    val fromOffice: OfficeDto? = null,
    val fromClient: ClientDto? = null,
    val fromAccountType: CodeDescriptionDto? = null,
    val fromAccount: StandingInstructionAccountDto? = null,

    // To
    val toOffice: OfficeDto? = null,
    val toClient: ClientDto? = null,
    val toAccountType: CodeDescriptionDto? = null,
    val toAccount: StandingInstructionAccountDto? = null,
)

// --- Sub DTOs ---

@Serializable
data class CodeDescriptionDto(
    val id: Int? = null,
    val code: String? = null,
    val description: String? = null,
)

@Serializable
data class OfficeDto(
    val id: Int? = null,
    val name: String? = null,
)

@Serializable
data class ClientDto(
    val id: Int? = null,
    val displayName: String? = null,
    val officeId: Int? = null,
    val officeName: String? = null,
)

@Serializable
data class StandingInstructionAccountDto(
    val id: Int? = null,
    val accountNo: String? = null,
    val productId: Int? = null,
    val productName: String? = null,
)
