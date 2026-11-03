/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
import kpt.core.model.datatable.DataTable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientsTemplateEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ClientsTemplate(
    /** The date the record was activated. */
    val activationDate: List<Int> = emptyList(),
    /** The server id of the office. */
    val officeId: Int = 0,
    /** The offices that may be chosen. */
    val officeOptions: List<OfficeOptions>? = emptyList(),
    /** The staff members that may be assigned. */
    val staffOptions: List<StaffOptions>? = emptyList(),
    /** The savings products that may be chosen. */
    val savingProductOptions: List<SavingProductOptions>? = emptyList(),
    /** The genders that may be chosen. */
    val genderOptions: List<Options>? = emptyList(),
    /** The client types that may be chosen. */
    val clientTypeOptions: List<Options>? = emptyList(),
    /** The client classifications that may be chosen. */
    val clientClassificationOptions: List<Options>? = emptyList(),
    /** The legal forms that may be chosen. */
    val clientLegalFormOptions: List<InterestTypeOption>? = emptyList(),
    /** The data tables registered for this entity. */
    val dataTables: List<DataTable>? = emptyList(),
)
