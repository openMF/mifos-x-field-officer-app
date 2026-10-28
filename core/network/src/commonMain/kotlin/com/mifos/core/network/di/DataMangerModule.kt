/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.network.di

import com.mifos.core.network.BaseApiManager
import com.mifos.core.network.DataManager
import com.mifos.core.network.datamanager.DataManagerAuth
import com.mifos.core.network.datamanager.DataManagerCenter
import com.mifos.core.network.datamanager.DataManagerCharge
import com.mifos.core.network.datamanager.DataManagerCheckerInbox
import com.mifos.core.network.datamanager.DataManagerClient
import com.mifos.core.network.datamanager.DataManagerCollectionSheet
import com.mifos.core.network.datamanager.DataManagerDataTable
import com.mifos.core.network.datamanager.DataManagerDocument
import com.mifos.core.network.datamanager.DataManagerFixedDeposit
import com.mifos.core.network.datamanager.DataManagerGroups
import com.mifos.core.network.datamanager.DataManagerIdentifiers
import com.mifos.core.network.datamanager.DataManagerLoan
import com.mifos.core.network.datamanager.DataManagerNote
import com.mifos.core.network.datamanager.DataManagerOffices
import com.mifos.core.network.datamanager.DataManagerRecurringAccount
import com.mifos.core.network.datamanager.DataManagerRunReport
import com.mifos.core.network.datamanager.DataManagerSavings
import com.mifos.core.network.datamanager.DataManagerSearch
import com.mifos.core.network.datamanager.DataManagerShare
import com.mifos.core.network.datamanager.DataManagerStaff
import com.mifos.core.network.datamanager.DataManagerStandingInstructions
import com.mifos.core.network.datamanager.DataManagerSurveys
import org.koin.dsl.module

val DataManagerModule = module {
    single { DataManager() }
    single { DataManagerAuth(get<BaseApiManager>()) }
    single { DataManagerCenter(get(), get(), get()) }
    single { DataManagerCharge(get(), get(), get()) }
    single { DataManagerCheckerInbox(get()) }
    single { DataManagerClient(get(), get(), get()) }
    single { DataManagerCollectionSheet(get()) }
    single { DataManagerDataTable(get()) }
    single { DataManagerDocument(get()) }
    single { DataManagerGroups(get(), get(), get(), get()) }
    single { DataManagerLoan(get(), get(), get()) }
    single { DataManagerNote(get()) }
    single { DataManagerOffices(get(), get(), get()) }
    single { DataManagerRunReport(get()) }
    single { DataManagerSavings(get(), get(), get()) }
    single { DataManagerSearch(get()) }
    single { DataManagerStaff(get(), get(), get()) }
    single { DataManagerSurveys(get(), get(), get()) }
    single { DataManagerIdentifiers(get()) }
    single { DataManagerRecurringAccount(get()) }
    single { DataManagerShare(get()) }
    single { DataManagerFixedDeposit(get()) }
    single { DataManagerStandingInstructions(get()) }
}
