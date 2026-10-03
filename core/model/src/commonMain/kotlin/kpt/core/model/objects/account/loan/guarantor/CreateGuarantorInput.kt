/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.guarantor

import kpt.core.model.utils.DateConstants

data class CreateGuarantorInput(
    val clientRelationshipTypeId: Long,
    val dateFormat: String = "dd-MM-yyyy",
    val entityId: Int? = null,
    val guarantorTypeId: Long,
    val locale: String = DateConstants.LOCALE,
    val firstname: String? = null,
    val lastname: String? = null,
    val dob: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
    val city: String? = null,
    val zip: String? = null,
    val mobileNumber: String? = null,
    val housePhoneNumber: String? = null,
)
