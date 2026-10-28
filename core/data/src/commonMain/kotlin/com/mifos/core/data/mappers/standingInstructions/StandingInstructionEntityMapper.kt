/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.data.mappers.standingInstructions

import com.mifos.core.model.objects.standingInstructions.StandingInstruction
import com.mifos.core.model.objects.standingInstructions.StandingInstructionAccount
import com.mifos.core.model.objects.standingInstructions.StandingInstructionClient
import com.mifos.core.network.dto.standingInstruction.StandingInstructionDto
import com.mifos.room.entities.standingInstructions.StandingInstructionAccountEntity
import com.mifos.room.entities.standingInstructions.StandingInstructionClientEntity
import com.mifos.room.entities.standingInstructions.StandingInstructionEntity

fun StandingInstructionDto.toEntity(): StandingInstructionEntity {
    return StandingInstructionEntity(
        id = this.id,
        amount = this.amount,
        validFrom = this.validFrom,
        fromClient = this.fromClient?.let { StandingInstructionClientEntity(it.id, it.displayName) },
        toClient = this.toClient?.let { StandingInstructionClientEntity(it.id, it.displayName) },
        fromAccount = this.fromAccount?.let { StandingInstructionAccountEntity(it.id, it.accountNo, it.productName) },
        toAccount = this.toAccount?.let {
            StandingInstructionAccountEntity(
                it.id,
                it.accountNo,
                it.productName,
            )
        },
    )
}

fun StandingInstructionEntity.toDomain(): StandingInstruction {
    return StandingInstruction(
        id = this.id,
        amount = this.amount,
        validFrom = this.validFrom,
        fromClient = this.fromClient?.toDomain(),
        toClient = this.toClient?.toDomain(),
        fromAccount = this.fromAccount?.toDomain(),
        toAccount = this.toAccount?.toDomain(),
    )
}

fun StandingInstructionClientEntity.toDomain(): StandingInstructionClient {
    return StandingInstructionClient(
        id = this.id,
        displayName = this.displayName,
    )
}

fun StandingInstructionAccountEntity.toDomain(): StandingInstructionAccount {
    return StandingInstructionAccount(
        id = this.id,
        accountNo = this.accountNo,
        productName = this.productName,
    )
}
