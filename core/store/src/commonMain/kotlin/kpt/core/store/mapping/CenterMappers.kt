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

import kpt.core.model.center.Center
import kpt.core.model.center.CenterAccounts
import kpt.core.model.center.CenterDate
import kpt.core.model.center.CenterPayload
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.collectionsheet.CenterDetail
import kpt.core.database.center.entity.CenterAccounts as CenterAccountsRow
import kpt.core.database.center.entity.CenterDateEntity as CenterDateRow
import kpt.core.database.center.entity.CenterEntity as CenterRow
import kpt.core.database.center.entity.CenterPayloadEntity as CenterPayloadRow
import kpt.core.database.center.entity.CenterWithAssociations as CenterWithAssociationsRow
import kpt.core.database.collectionsheet.entity.CenterDetail as CenterDetailRow

/** Maps the stored row to its `CenterDate` model. */
fun CenterDateRow.toModel(): CenterDate = CenterDate(
    centerId = centerId,
    chargeId = chargeId,
    day = day,
    month = month,
    year = year,
)

/** Maps the model to its stored `CenterDateRow`. */
fun CenterDate.toEntity(): CenterDateRow = CenterDateRow(
    centerId = centerId,
    chargeId = chargeId,
    day = day,
    month = month,
    year = year,
)

/** Maps the stored row to its `Center` model. */
fun CenterRow.toModel(): Center = Center(
    id = id,
    sync = sync,
    accountNo = accountNo,
    name = name,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    status = status?.toModel(),
    active = active,
    centerDate = centerDate?.toModel(),
    activationDate = activationDate,
    timeline = timeline?.toModel(),
    externalId = externalId,
)

/** Maps the model to its stored `CenterRow`. */
fun Center.toEntity(): CenterRow = CenterRow(
    id = id,
    sync = sync,
    accountNo = accountNo,
    name = name,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    status = status?.toEntity(),
    active = active,
    centerDate = centerDate?.toEntity(),
    activationDate = activationDate,
    timeline = timeline?.toEntity(),
    externalId = externalId,
)

/** Maps the stored row to its `CenterPayload` model. */
fun CenterPayloadRow.toModel(): CenterPayload = CenterPayload(
    id = id,
    errorMessage = errorMessage,
    dateFormat = dateFormat,
    locale = locale,
    name = name,
    officeId = officeId,
    active = active,
    activationDate = activationDate,
)

/** Maps the model to its stored `CenterPayloadRow`. */
fun CenterPayload.toEntity(): CenterPayloadRow = CenterPayloadRow(
    id = id,
    errorMessage = errorMessage,
    dateFormat = dateFormat,
    locale = locale,
    name = name,
    officeId = officeId,
    active = active,
    activationDate = activationDate,
)

/** Maps the stored row to its `CenterDetail` model. */
fun CenterDetailRow.toModel(): CenterDetail = CenterDetail(
    staffId = staffId,
    staffName = staffName,
    meetingFallCenters = meetingFallCenters?.map { it.toModel() },
)

/** Maps the model to its stored `CenterDetailRow`. */
fun CenterDetail.toEntity(): CenterDetailRow = CenterDetailRow(
    staffId = staffId,
    staffName = staffName,
    meetingFallCenters = meetingFallCenters?.map { it.toEntity() },
)

/** Maps the stored row to its `CenterAccounts` model. */
fun CenterAccountsRow.toModel(): CenterAccounts = CenterAccounts(
    loanAccounts = loanAccounts.map { it.toModel() },
    savingsAccounts = savingsAccounts.map { it.toModel() },
    memberLoanAccounts = memberLoanAccounts.map { it.toModel() },
)

/** Maps the model to its stored `CenterAccountsRow`. */
fun CenterAccounts.toEntity(): CenterAccountsRow = CenterAccountsRow(
    loanAccounts = loanAccounts.map { it.toEntity() },
    savingsAccounts = savingsAccounts.map { it.toEntity() },
    memberLoanAccounts = memberLoanAccounts.map { it.toEntity() },
)

/** Maps the stored row to its `CenterWithAssociations` model. */
fun CenterWithAssociationsRow.toModel(): CenterWithAssociations = CenterWithAssociations(
    id = id,
    accountNo = accountNo,
    name = name,
    externalId = externalId,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    status = status?.toModel(),
    active = active,
    activationDate = activationDate,
    timeline = timeline,
    groupMembers = groupMembers.map { it.toModel() },
    collectionMeetingCalendar = collectionMeetingCalendar,
)

/** Maps the model to its stored `CenterWithAssociationsRow`. */
fun CenterWithAssociations.toEntity(): CenterWithAssociationsRow = CenterWithAssociationsRow(
    id = id,
    accountNo = accountNo,
    name = name,
    externalId = externalId,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    status = status?.toEntity(),
    active = active,
    activationDate = activationDate,
    timeline = timeline,
    groupMembers = groupMembers.map { it.toEntity() },
    collectionMeetingCalendar = collectionMeetingCalendar,
)
