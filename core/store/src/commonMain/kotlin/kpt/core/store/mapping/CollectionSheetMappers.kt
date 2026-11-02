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

import kpt.core.model.collectionsheet.ClientCollectionSheet
import kpt.core.model.collectionsheet.CollectionSheetResponse
import kpt.core.model.collectionsheet.GroupCollectionSheet
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.MeetingFallCalendar
import kpt.core.model.collectionsheet.SavingsCollectionSheet
import kpt.core.database.collectionsheet.entity.ClientCollectionSheet as ClientCollectionSheetRow
import kpt.core.database.collectionsheet.entity.CollectionSheetResponse as CollectionSheetResponseRow
import kpt.core.database.collectionsheet.entity.GroupCollectionSheet as GroupCollectionSheetRow
import kpt.core.database.collectionsheet.entity.IndividualCollectionSheet as IndividualCollectionSheetRow
import kpt.core.database.collectionsheet.entity.MeetingFallCalendar as MeetingFallCalendarRow
import kpt.core.database.collectionsheet.entity.SavingsCollectionSheet as SavingsCollectionSheetRow

fun CollectionSheetResponseRow.toModel(): CollectionSheetResponse = CollectionSheetResponse(
    attendanceTypeOptions = attendanceTypeOptions,
    dueDate = dueDate,
    groups = groups.map { it.toModel() },
    loanProducts = loanProducts,
    paymentTypeOptions = paymentTypeOptions.map { it.toModel() },
    savingsProducts = savingsProducts,
)

fun CollectionSheetResponse.toEntity(): CollectionSheetResponseRow = CollectionSheetResponseRow(
    attendanceTypeOptions = attendanceTypeOptions,
    dueDate = dueDate,
    groups = groups.map { it.toEntity() },
    loanProducts = loanProducts,
    paymentTypeOptions = paymentTypeOptions.map { it.toEntity() },
    savingsProducts = savingsProducts,
)

fun IndividualCollectionSheetRow.toModel(): IndividualCollectionSheet = IndividualCollectionSheet(
    dueDate = dueDate,
    clients = clients?.mapTo(ArrayList()) { it.toModel() },
    paymentTypeOptions = paymentTypeOptions,
)

fun IndividualCollectionSheet.toEntity(): IndividualCollectionSheetRow = IndividualCollectionSheetRow(
    dueDate = dueDate,
    clients = clients?.mapTo(ArrayList()) { it.toEntity() },
    paymentTypeOptions = paymentTypeOptions,
)

fun GroupCollectionSheetRow.toModel(): GroupCollectionSheet = GroupCollectionSheet(
    clients = clients.mapTo(ArrayList()) { it.toModel() },
    groupId = groupId,
    groupName = groupName,
    levelId = levelId,
    levelName = levelName,
    staffId = staffId,
    staffName = staffName,
)

fun GroupCollectionSheet.toEntity(): GroupCollectionSheetRow = GroupCollectionSheetRow(
    clients = clients.mapTo(ArrayList()) { it.toEntity() },
    groupId = groupId,
    groupName = groupName,
    levelId = levelId,
    levelName = levelName,
    staffId = staffId,
    staffName = staffName,
)

fun ClientCollectionSheetRow.toModel(): ClientCollectionSheet = ClientCollectionSheet(
    clientId = clientId,
    clientName = clientName,
    loans = loans,
    attendanceType = attendanceType,
    savings = savings.mapTo(ArrayList()) { it.toModel() },
)

fun ClientCollectionSheet.toEntity(): ClientCollectionSheetRow = ClientCollectionSheetRow(
    clientId = clientId,
    clientName = clientName,
    loans = loans,
    attendanceType = attendanceType,
    savings = savings.mapTo(ArrayList()) { it.toEntity() },
)

fun SavingsCollectionSheetRow.toModel(): SavingsCollectionSheet = SavingsCollectionSheet(
    accountId = accountId,
    accountStatusId = accountStatusId,
    currency = currency?.toModel(),
    depositAccountType = depositAccountType,
    dueAmount = dueAmount,
    productId = productId,
    productName = productName,
    savingsId = savingsId,
)

fun SavingsCollectionSheet.toEntity(): SavingsCollectionSheetRow = SavingsCollectionSheetRow(
    accountId = accountId,
    accountStatusId = accountStatusId,
    currency = currency?.toEntity(),
    depositAccountType = depositAccountType,
    dueAmount = dueAmount,
    productId = productId,
    productName = productName,
    savingsId = savingsId,
)

fun MeetingFallCalendarRow.toModel(): MeetingFallCalendar = MeetingFallCalendar(
    accountNo = accountNo,
    activationDate = activationDate,
    isActive = isActive,
    collectionMeetingCalendar = collectionMeetingCalendar,
    hierarchy = hierarchy,
    id = id,
    installmentDue = installmentDue,
    name = name,
    officeId = officeId,
    staffId = staffId,
    staffName = staffName,
    status = status?.toModel(),
    totalCollected = totalCollected,
    totalOverdue = totalOverdue,
    totaldue = totaldue,
)

fun MeetingFallCalendar.toEntity(): MeetingFallCalendarRow = MeetingFallCalendarRow(
    accountNo = accountNo,
    activationDate = activationDate,
    isActive = isActive,
    collectionMeetingCalendar = collectionMeetingCalendar,
    hierarchy = hierarchy,
    id = id,
    installmentDue = installmentDue,
    name = name,
    officeId = officeId,
    staffId = staffId,
    staffName = staffName,
    status = status?.toEntity(),
    totalCollected = totalCollected,
    totalOverdue = totalOverdue,
    totaldue = totaldue,
)
