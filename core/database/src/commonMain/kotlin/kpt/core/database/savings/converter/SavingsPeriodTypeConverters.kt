/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.database.savings.entity.SavingsAccountStatusEntity
import kpt.core.database.savings.entity.SavingsAccountSummaryEntity
import kpt.core.model.objects.account.saving.InterestPostingPeriodType
import kpt.core.model.objects.account.saving.LockinPeriodFrequencyType

@DbConverters
class SavingsPeriodTypeConverters {
    @ColumnTypeConverter
    fun fromInterestPostingPeriodType(
        type: InterestPostingPeriodType?,
    ): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toInterestPostingPeriodType(
        json: String?,
    ): InterestPostingPeriodType? {
        return json?.let { Json.decodeFromString<InterestPostingPeriodType>(it) }
    }

    @ColumnTypeConverter
    fun fromLockinPeriodFrequencyType(type: LockinPeriodFrequencyType?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toLockinPeriodFrequencyType(json: String?): LockinPeriodFrequencyType? {
        return json?.let { Json.decodeFromString<LockinPeriodFrequencyType>(it) }
    }

    @ColumnTypeConverter
    fun fromPaymentTypeOption(paymentTypeOption: PaymentTypeOptionEntity?): String? {
        return paymentTypeOption?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toPaymentTypeOption(json: String?): PaymentTypeOptionEntity? {
        return json?.let { Json.decodeFromString<PaymentTypeOptionEntity>(it) }
    }

    @ColumnTypeConverter
    fun fromSavingAccountStatus(type: SavingsAccountStatusEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toSavingAccountStatus(json: String?): SavingsAccountStatusEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromSavingAccountSummary(type: SavingsAccountSummaryEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toSavingAccountSummary(json: String?): SavingsAccountSummaryEntity? {
        return json?.let { Json.decodeFromString(it) }
    }
}
