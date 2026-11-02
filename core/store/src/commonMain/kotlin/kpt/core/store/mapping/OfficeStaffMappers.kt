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

import kpt.core.model.client.OfficeOptions
import kpt.core.model.client.Options
import kpt.core.model.client.StaffOptions
import kpt.core.model.office.Office
import kpt.core.model.office.OfficeOpeningDate
import kpt.core.model.staff.Staff
import kpt.core.database.client.entity.OfficeOptionsEntity as OfficeOptionsRow
import kpt.core.database.client.entity.OptionsEntity as OptionsRow
import kpt.core.database.client.entity.StaffOptionsEntity as StaffOptionsRow
import kpt.core.database.office.entity.OfficeEntity as OfficeRow
import kpt.core.database.office.entity.OfficeOpeningDateEntity as OfficeOpeningDateRow
import kpt.core.database.staff.entity.StaffEntity as StaffRow

fun OfficeRow.toModel(): Office = Office(
    id = id,
    externalId = externalId,
    name = name,
    nameDecorated = nameDecorated,
    officeOpeningDate = officeOpeningDate?.toModel(),
    openingDate = openingDate,
)

fun Office.toEntity(): OfficeRow = OfficeRow(
    id = id,
    externalId = externalId,
    name = name,
    nameDecorated = nameDecorated,
    officeOpeningDate = officeOpeningDate?.toEntity(),
    openingDate = openingDate,
)

fun OfficeOpeningDateRow.toModel(): OfficeOpeningDate = OfficeOpeningDate(
    officeId = officeId,
    year = year,
    month = month,
    day = day,
)

fun OfficeOpeningDate.toEntity(): OfficeOpeningDateRow = OfficeOpeningDateRow(
    officeId = officeId,
    year = year,
    month = month,
    day = day,
)

fun OfficeOptionsRow.toModel(): OfficeOptions = OfficeOptions(
    id = id,
    name = name,
    nameDecorated = nameDecorated,
)

fun OfficeOptions.toEntity(): OfficeOptionsRow = OfficeOptionsRow(
    id = id,
    name = name,
    nameDecorated = nameDecorated,
)

fun OptionsRow.toModel(): Options = Options(
    optionType = optionType,
    id = id,
    name = name,
    position = position,
    description = description,
    activeStatus = activeStatus,
)

fun Options.toEntity(): OptionsRow = OptionsRow(
    optionType = optionType,
    id = id,
    name = name,
    position = position,
    description = description,
    activeStatus = activeStatus,
)

fun StaffRow.toModel(): Staff = Staff(
    id = id,
    firstname = firstname,
    lastname = lastname,
    mobileNo = mobileNo,
    displayName = displayName,
    officeId = officeId,
    officeName = officeName,
    isLoanOfficer = isLoanOfficer,
    isActive = isActive,
)

fun Staff.toEntity(): StaffRow = StaffRow(
    id = id,
    firstname = firstname,
    lastname = lastname,
    mobileNo = mobileNo,
    displayName = displayName,
    officeId = officeId,
    officeName = officeName,
    isLoanOfficer = isLoanOfficer,
    isActive = isActive,
)

fun StaffOptionsRow.toModel(): StaffOptions = StaffOptions(
    id = id,
    firstname = firstname,
    lastname = lastname,
    displayName = displayName,
    officeId = officeId,
    officeName = officeName,
    isLoanOfficer = isLoanOfficer,
    isActive = isActive,
)

fun StaffOptions.toEntity(): StaffOptionsRow = StaffOptionsRow(
    id = id,
    firstname = firstname,
    lastname = lastname,
    displayName = displayName,
    officeId = officeId,
    officeName = officeName,
    isLoanOfficer = isLoanOfficer,
    isActive = isActive,
)
