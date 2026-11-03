/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.mapping

import kpt.core.model.client.AddressConfiguration
import kpt.core.model.client.AddressTemplate
import kpt.core.model.client.ClientClassification
import kpt.core.model.client.ClientGender
import kpt.core.model.client.ClientType
import kpt.core.model.client.ClientsTemplate
import kpt.core.database.client.entity.AddressConfiguration as AddressConfigurationRow
import kpt.core.database.client.entity.AddressTemplate as AddressTemplateRow
import kpt.core.database.client.entity.ClientClassificationEntity as ClientClassificationRow
import kpt.core.database.client.entity.ClientGenderEntity as ClientGenderRow
import kpt.core.database.client.entity.ClientTypeEntity as ClientTypeRow
import kpt.core.database.client.entity.ClientsTemplateEntity as ClientsTemplateRow

/** Maps the stored row to its `AddressTemplate` model. */
fun AddressTemplateRow.toModel(): AddressTemplate = AddressTemplate(
    addressTypeIdOptions = addressTypeIdOptions.map { it.toModel() },
    countryIdOptions = countryIdOptions.map { it.toModel() },
    stateProvinceIdOptions = stateProvinceIdOptions.map { it.toModel() },
)

/** Maps the model to its stored `AddressTemplateRow`. */
fun AddressTemplate.toEntity(): AddressTemplateRow = AddressTemplateRow(
    addressTypeIdOptions = addressTypeIdOptions.map { it.toEntity() },
    countryIdOptions = countryIdOptions.map { it.toEntity() },
    stateProvinceIdOptions = stateProvinceIdOptions.map { it.toEntity() },
)

/** Maps the stored row to its `ClientClassification` model. */
fun ClientClassificationRow.toModel(): ClientClassification = ClientClassification(
    id = id,
    name = name,
)

/** Maps the model to its stored `ClientClassificationRow`. */
fun ClientClassification.toEntity(): ClientClassificationRow = ClientClassificationRow(
    id = id,
    name = name,
)

/** Maps the stored row to its `ClientGender` model. */
fun ClientGenderRow.toModel(): ClientGender = ClientGender(
    id = id,
    name = name,
)

/** Maps the model to its stored `ClientGenderRow`. */
fun ClientGender.toEntity(): ClientGenderRow = ClientGenderRow(
    id = id,
    name = name,
)

/** Maps the stored row to its `ClientType` model. */
fun ClientTypeRow.toModel(): ClientType = ClientType(
    id = id,
    name = name,
)

/** Maps the model to its stored `ClientTypeRow`. */
fun ClientType.toEntity(): ClientTypeRow = ClientTypeRow(
    id = id,
    name = name,
)

/** Maps the stored row to its `ClientsTemplate` model. */
fun ClientsTemplateRow.toModel(): ClientsTemplate = ClientsTemplate(
    activationDate = activationDate,
    officeId = officeId,
    officeOptions = officeOptions?.map { it.toModel() },
    staffOptions = staffOptions?.map { it.toModel() },
    savingProductOptions = savingProductOptions?.map { it.toModel() },
    genderOptions = genderOptions?.map { it.toModel() },
    clientTypeOptions = clientTypeOptions?.map { it.toModel() },
    clientClassificationOptions = clientClassificationOptions?.map { it.toModel() },
    clientLegalFormOptions = clientLegalFormOptions?.map { it.toModel() },
    dataTables = dataTables?.map { it.toModel() },
)

/** Maps the model to its stored `ClientsTemplateRow`. */
fun ClientsTemplate.toEntity(): ClientsTemplateRow = ClientsTemplateRow(
    activationDate = activationDate,
    officeId = officeId,
    officeOptions = officeOptions?.map { it.toEntity() },
    staffOptions = staffOptions?.map { it.toEntity() },
    savingProductOptions = savingProductOptions?.map { it.toEntity() },
    genderOptions = genderOptions?.map { it.toEntity() },
    clientTypeOptions = clientTypeOptions?.map { it.toEntity() },
    clientClassificationOptions = clientClassificationOptions?.map { it.toEntity() },
    clientLegalFormOptions = clientLegalFormOptions?.map { it.toEntity() },
    dataTables = dataTables?.map { it.toEntity() },
)

/** Maps the stored row to its `AddressConfiguration` model. */
fun AddressConfigurationRow.toModel(): AddressConfiguration = AddressConfiguration(
    enabled = enabled,
)

/** Maps the model to its stored `AddressConfigurationRow`. */
fun AddressConfiguration.toEntity(): AddressConfigurationRow = AddressConfigurationRow(
    enabled = enabled,
)
