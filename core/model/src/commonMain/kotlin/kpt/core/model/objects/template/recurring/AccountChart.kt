/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.recurring.incentive.IncentiveTypeOption
import kpt.core.model.objects.template.recurring.period.PeriodType
/** An account chart. */
@Serializable
data class AccountChart(
    /** The client attributes incentives may depend on. */
    val attributeNameOptions: List<AttributeNameOption>? = null,
    /** The rate slabs of the chart. */
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    /** The client types that may be chosen. */
    val clientTypeOptions: List<ClientTypeOption>? = null,
    /** The comparisons incentives may use. */
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    /** The date this ends. */
    val endDate: List<Int>? = null,
    /** The entity types the chart may be attached to. */
    val entityTypeOptions: List<EntityTypeOption>? = null,
    /** The date this starts from. */
    val fromDate: List<Int>? = null,
//    val genderOptions: List<Any?>? = null,
    /** The incentive types that may be chosen. */
    val incentiveTypeOptions: List<IncentiveTypeOption>? = null,
    /** True when the chart groups its slabs by amount before period. */
    val isPrimaryGroupingByAmount: Boolean? = null,
    /** The display name. */
    val name: String? = null,
    /** A free-text description. */
    val description: String? = null,
    /** The period units the chart may use. */
    val periodTypes: List<PeriodType>? = null,
)
