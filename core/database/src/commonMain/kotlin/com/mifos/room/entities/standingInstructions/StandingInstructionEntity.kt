/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.room.entities.standingInstructions

import template.core.base.database.Entity
import template.core.base.database.PrimaryKey

@Entity(
    tableName = "StandingInstruction",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    foreignKeys = [],
)
data class StandingInstructionEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    val amount: Double? = null,

    val validFrom: String? = null,

    val fromClient: StandingInstructionClientEntity? = null,

    val toClient: StandingInstructionClientEntity? = null,

    val fromAccount: StandingInstructionAccountEntity? = null,

    val toAccount: StandingInstructionAccountEntity? = null,
)
