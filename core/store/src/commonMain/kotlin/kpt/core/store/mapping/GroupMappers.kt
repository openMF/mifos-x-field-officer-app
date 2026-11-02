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

import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupDate
import kpt.core.model.group.GroupPayload
import kpt.core.model.group.GroupWithAssociations
import kpt.core.database.group.entity.GroupAccounts as GroupAccountsRow
import kpt.core.database.group.entity.GroupDateEntity as GroupDateRow
import kpt.core.database.group.entity.GroupEntity as GroupRow
import kpt.core.database.group.entity.GroupPayloadEntity as GroupPayloadRow
import kpt.core.database.group.entity.GroupWithAssociations as GroupWithAssociationsRow

fun GroupDateRow.toModel(): GroupDate = GroupDate(
    groupId = groupId,
    chargeId = chargeId,
    day = day,
    month = month,
    year = year,
)

fun GroupDate.toEntity(): GroupDateRow = GroupDateRow(
    groupId = groupId,
    chargeId = chargeId,
    day = day,
    month = month,
    year = year,
)

fun GroupRow.toModel(): Group = Group(
    id = id,
    accountNo = accountNo,
    sync = sync,
    name = name,
    status = status?.toModel(),
    active = active,
    groupDate = groupDate?.toModel(),
    activationDate = activationDate,
    officeId = officeId,
    officeName = officeName,
    centerId = centerId,
    centerName = centerName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    groupLevel = groupLevel,
    timeline = timeline,
    externalId = externalId,
)

fun Group.toEntity(): GroupRow = GroupRow(
    id = id,
    accountNo = accountNo,
    sync = sync,
    name = name,
    status = status?.toEntity(),
    active = active,
    groupDate = groupDate?.toEntity(),
    activationDate = activationDate,
    officeId = officeId,
    officeName = officeName,
    centerId = centerId,
    centerName = centerName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    groupLevel = groupLevel,
    timeline = timeline,
    externalId = externalId,
)

fun GroupPayloadRow.toModel(): GroupPayload = GroupPayload(
    id = id,
    errorMessage = errorMessage,
    officeId = officeId,
    active = active,
    activationDate = activationDate,
    submittedOnDate = submittedOnDate,
    externalId = externalId,
    name = name,
    locale = locale,
    dateFormat = dateFormat,
)

fun GroupPayload.toEntity(): GroupPayloadRow = GroupPayloadRow(
    id = id,
    errorMessage = errorMessage,
    officeId = officeId,
    active = active,
    activationDate = activationDate,
    submittedOnDate = submittedOnDate,
    externalId = externalId,
    name = name,
    locale = locale,
    dateFormat = dateFormat,
)

fun GroupAccountsRow.toModel(): GroupAccounts = GroupAccounts(
    loanAccounts = loanAccounts.map { it.toModel() },
    savingsAccounts = savingsAccounts.map { it.toModel() },
)

fun GroupAccounts.toEntity(): GroupAccountsRow = GroupAccountsRow(
    loanAccounts = loanAccounts.map { it.toEntity() },
    savingsAccounts = savingsAccounts.map { it.toEntity() },
)

fun GroupWithAssociationsRow.toModel(): GroupWithAssociations = GroupWithAssociations(
    id = id,
    accountNo = accountNo,
    name = name,
    status = status?.toModel(),
    active = active,
    activationDate = activationDate,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    groupLevel = groupLevel,
    clientMembers = clientMembers.map { it.toModel() },
    timeline = timeline,
)

fun GroupWithAssociations.toEntity(): GroupWithAssociationsRow = GroupWithAssociationsRow(
    id = id,
    accountNo = accountNo,
    name = name,
    status = status?.toEntity(),
    active = active,
    activationDate = activationDate,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    groupLevel = groupLevel,
    clientMembers = clientMembers.map { it.toEntity() },
    timeline = timeline,
)
