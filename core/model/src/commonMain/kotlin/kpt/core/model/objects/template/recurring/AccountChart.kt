/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.recurring.incentive.IncentiveTypeOption
import kpt.core.model.objects.template.recurring.period.PeriodType
@Serializable
data class AccountChart(
    val attributeNameOptions: List<AttributeNameOption>? = null,
    val chartSlabs: List<ChartSlab>? = null,
//    val clientClassificationOptions: List<Any?>? = null,
    val clientTypeOptions: List<ClientTypeOption>? = null,
    val conditionTypeOptions: List<ConditionTypeOption>? = null,
    val endDate: List<Int>? = null,
    val entityTypeOptions: List<EntityTypeOption>? = null,
    val fromDate: List<Int>? = null,
//    val genderOptions: List<Any?>? = null,
    val incentiveTypeOptions: List<IncentiveTypeOption>? = null,
    val isPrimaryGroupingByAmount: Boolean? = null,
    val name: String? = null,
    val description: String? = null,
    val periodTypes: List<PeriodType>? = null,
)
