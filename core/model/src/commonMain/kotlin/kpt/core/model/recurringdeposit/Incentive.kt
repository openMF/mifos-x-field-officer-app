/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable

/** An incentive. */
@Serializable
data class Incentive(
    /** The amount, in the record's currency. */
    val amount: Double? = null,
    /** The client attribute the incentive depends on. */
    val attributeName: AttributeName? = null,
    /** The attribute value the incentive applies to. */
    val attributeValue: String? = null,
    /** The description of that attribute value. */
    val attributeValueDesc: String? = null,
    /** The comparison the incentive uses (equal, greater than, …). */
    val conditionType: ConditionType? = null,
    /** The kind of entity this record belongs to (client, group, loan, …). */
    val entityType: EntityType? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** How the incentive changes the rate (fixed, increase, decrease). */
    val incentiveType: IncentiveType? = null,
)
