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

import kpt.core.model.charge.ClientChargeCurrency
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.client.ClientDate
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.ClientStatus
import kpt.core.database.charge.entity.ClientChargeCurrencyEntity as ClientChargeCurrencyRow
import kpt.core.database.client.entity.ClientAccounts as ClientAccountsRow
import kpt.core.database.client.entity.ClientDateEntity as ClientDateRow
import kpt.core.database.client.entity.ClientEntity as ClientRow
import kpt.core.database.client.entity.ClientPayloadEntity as ClientPayloadRow
import kpt.core.database.client.entity.ClientStatusEntity as ClientStatusRow

/*
 * The core/database ↔ core/model boundary. GENERATED from the entity constructors (one pair per
 * type); field names are identical on both sides, so a field added to a row and not to its model
 * fails to compile here rather than being dropped silently.
 *
 * Entities never leave core/store: stores read rows and emit these models, and an offline store's
 * writer converts a model back to its row. Features see only core/model (Store5 gate
 * S5-FEATURE-DEP-DIRECTION).
 */

/** Maps the stored row to its `ClientAccounts` model. */
fun ClientAccountsRow.toModel(): ClientAccounts = ClientAccounts(
    loanAccounts = loanAccounts.map { it.toModel() },
    savingsAccounts = savingsAccounts.map { it.toModel() },
    shareAccounts = shareAccounts,
)

/** Maps the model to its stored `ClientAccountsRow`. */
fun ClientAccounts.toEntity(): ClientAccountsRow = ClientAccountsRow(
    loanAccounts = loanAccounts.map { it.toEntity() },
    savingsAccounts = savingsAccounts.map { it.toEntity() },
    shareAccounts = shareAccounts,
)

/** Maps the stored row to its `ClientChargeCurrency` model. */
fun ClientChargeCurrencyRow.toModel(): ClientChargeCurrency = ClientChargeCurrency(
    id = id,
    code = code,
    name = name,
    decimalPlaces = decimalPlaces,
    inMultiplesOf = inMultiplesOf,
    displaySymbol = displaySymbol,
    nameCode = nameCode,
    displayLabel = displayLabel,
)

/** Maps the model to its stored `ClientChargeCurrencyRow`. */
fun ClientChargeCurrency.toEntity(): ClientChargeCurrencyRow = ClientChargeCurrencyRow(
    id = id,
    code = code,
    name = name,
    decimalPlaces = decimalPlaces,
    inMultiplesOf = inMultiplesOf,
    displaySymbol = displaySymbol,
    nameCode = nameCode,
    displayLabel = displayLabel,
)

/** Maps the stored row to its `ClientDate` model. */
fun ClientDateRow.toModel(): ClientDate = ClientDate(
    clientId = clientId,
    chargeId = chargeId,
    day = day,
    month = month,
    year = year,
)

/** Maps the model to its stored `ClientDateRow`. */
fun ClientDate.toEntity(): ClientDateRow = ClientDateRow(
    clientId = clientId,
    chargeId = chargeId,
    day = day,
    month = month,
    year = year,
)

/** Maps the stored row to its `Client` model. */
fun ClientRow.toModel(): Client = Client(
    id = id,
    groupId = groupId,
    groupName = groupName,
    accountNo = accountNo,
    clientId = clientId,
    status = status?.toModel(),
    sync = sync,
    active = active,
    clientDate = clientDate?.toModel(),
    activationDate = activationDate,
    dateOfBirth = dateOfBirth,
    groups = groups?.map { it.toModel() },
    mobileNo = mobileNo,
    firstname = firstname,
    middlename = middlename,
    lastname = lastname,
    displayName = displayName,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    timeline = timeline,
    fullname = fullname,
    savingsAccountId = savingsAccountId,
    imageId = imageId,
    imagePresent = imagePresent,
    externalId = externalId,
    emailAddress = emailAddress,
    legalForm = legalForm?.toModel(),
    gender = gender?.toModel(),
    clientType = clientType?.toModel(),
    clientClassification = clientClassification?.toModel(),
)

/** Maps the model to its stored `ClientRow`. */
fun Client.toEntity(): ClientRow = ClientRow(
    id = id,
    groupId = groupId,
    groupName = groupName,
    accountNo = accountNo,
    clientId = clientId,
    status = status?.toEntity(),
    sync = sync,
    active = active,
    clientDate = clientDate?.toEntity(),
    activationDate = activationDate,
    dateOfBirth = dateOfBirth,
    groups = groups?.map { it.toEntity() },
    mobileNo = mobileNo,
    firstname = firstname,
    middlename = middlename,
    lastname = lastname,
    displayName = displayName,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    timeline = timeline,
    fullname = fullname,
    savingsAccountId = savingsAccountId,
    imageId = imageId,
    imagePresent = imagePresent,
    externalId = externalId,
    emailAddress = emailAddress,
    legalForm = legalForm?.toEntity(),
    gender = gender?.toEntity(),
    clientType = clientType?.toEntity(),
    clientClassification = clientClassification?.toEntity(),
)

/** Maps the stored row to its `ClientPayload` model. */
fun ClientPayloadRow.toModel(): ClientPayload = ClientPayload(
    id = id,
    clientCreationTime = clientCreationTime,
    errorMessage = errorMessage,
    firstname = firstname,
    lastname = lastname,
    middlename = middlename,
    emailAddress = emailAddress,
    officeId = officeId,
    staffId = staffId,
    genderId = genderId,
    active = active,
    activationDate = activationDate,
    submittedOnDate = submittedOnDate,
    dateOfBirth = dateOfBirth,
    mobileNo = mobileNo,
    externalId = externalId,
    clientTypeId = clientTypeId,
    clientClassificationId = clientClassificationId,
    address = address,
    dateFormat = dateFormat,
    locale = locale,
    datatables = datatables?.map { it.toModel() },
    legalFormId = legalFormId,
)

/** Maps the model to its stored `ClientPayloadRow`. */
fun ClientPayload.toEntity(): ClientPayloadRow = ClientPayloadRow(
    id = id,
    clientCreationTime = clientCreationTime,
    errorMessage = errorMessage,
    firstname = firstname,
    lastname = lastname,
    middlename = middlename,
    emailAddress = emailAddress,
    officeId = officeId,
    staffId = staffId,
    genderId = genderId,
    active = active,
    activationDate = activationDate,
    submittedOnDate = submittedOnDate,
    dateOfBirth = dateOfBirth,
    mobileNo = mobileNo,
    externalId = externalId,
    clientTypeId = clientTypeId,
    clientClassificationId = clientClassificationId,
    address = address,
    dateFormat = dateFormat,
    locale = locale,
    datatables = datatables?.map { it.toEntity() },
    legalFormId = legalFormId,
)

/** Maps the stored row to its `ClientStatus` model. */
fun ClientStatusRow.toModel(): ClientStatus = ClientStatus(
    id = id,
    code = code,
    value = value,
)

/** Maps the model to its stored `ClientStatusRow`. */
fun ClientStatus.toEntity(): ClientStatusRow = ClientStatusRow(
    id = id,
    code = code,
    value = value,
)
