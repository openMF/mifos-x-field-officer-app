/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.model.objects.standingInstructions

import kotlinx.serialization.Serializable

@Serializable
data class StandingInstruction(
    val id: Int,
    val amount: Double? = null,
    val validFrom: String? = null,
    val fromClient: StandingInstructionClient? = null,
    val toClient: StandingInstructionClient? = null,
    val fromAccount: StandingInstructionAccount? = null,
    val toAccount: StandingInstructionAccount? = null,
)
