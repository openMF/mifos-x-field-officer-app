/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.collectionsheet

import kotlinx.serialization.Serializable
import kpt.core.model.client.ClientsAttendance
import kpt.core.model.objects.collectionsheets.BulkSavingsDueTransaction
import kpt.core.model.shared.BulkRepaymentTransactions
import kpt.core.model.utils.ApiDateFormatter

/**
 * Created by Tarun on 31-07-17.
 */
@Serializable
data class CollectionSheetPayload(
    /** The date the loan was actually disbursed. */
    var actualDisbursementDate: String? = null,

    /** The loan repayments collected at the meeting. */
    var bulkRepaymentTransactions: MutableList<BulkRepaymentTransactions> = ArrayList(),
    /** The savings deposits collected at the meeting. */
    var bulkSavingsDueTransactions: MutableList<BulkSavingsDueTransaction> = ArrayList(),

    /** The server id of the meeting calendar. */
    var calendarId: Int? = 0,

    /** The attendance recorded for each client. */
    var clientsAttendance: MutableList<ClientsAttendance> = ArrayList(),

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String = ApiDateFormatter.DATE_FORMAT,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String = ApiDateFormatter.LOCALE,

    /** The date of the transaction. */
    var transactionDate: String? = null,

    /** The account number the payment was made from or to. */
    var accountNumber: String? = null,

    /** The bank number of the payment. */
    var bankNumber: String? = null,

    /** The cheque number of the payment. */
    var checkNumber: String? = null,

    /** The server id of the payment type. */
    var paymentTypeId: Int = 0,

    /** The receipt number of the payment. */
    var receiptNumber: String? = null,

    /** The routing code of the payment. */
    var routingCode: String? = null,
)
