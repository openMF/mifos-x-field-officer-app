/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.network

import com.mifos.core.common.utils.getInstanceUrl
import com.mifos.core.datastore.UserPreferencesRepository
import com.mifos.core.network.apis.CentersApi
import com.mifos.core.network.apis.ClientApi
import com.mifos.core.network.apis.ClientIdentifierApi
import com.mifos.core.network.apis.DataTablesApi
import com.mifos.core.network.apis.GroupsApi
import com.mifos.core.network.apis.OfficesApi
import com.mifos.core.network.apis.StaffApi
import com.mifos.core.network.apis.createCentersApi
import com.mifos.core.network.apis.createClientApi
import com.mifos.core.network.apis.createClientIdentifierApi
import com.mifos.core.network.apis.createDataTablesApi
import com.mifos.core.network.apis.createGroupsApi
import com.mifos.core.network.apis.createOfficesApi
import com.mifos.core.network.apis.createStaffApi
import com.mifos.core.network.services.CenterService
import com.mifos.core.network.services.ChargeService
import com.mifos.core.network.services.CheckerInboxService
import com.mifos.core.network.services.ClientAccountsService
import com.mifos.core.network.services.ClientService
import com.mifos.core.network.services.CollectionSheetService
import com.mifos.core.network.services.DataTableService
import com.mifos.core.network.services.DocumentService
import com.mifos.core.network.services.FixedDepositService
import com.mifos.core.network.services.GroupService
import com.mifos.core.network.services.LoanService
import com.mifos.core.network.services.NoteService
import com.mifos.core.network.services.OfficeService
import com.mifos.core.network.services.RecurringAccountService
import com.mifos.core.network.services.RunReportsService
import com.mifos.core.network.services.SavingsAccountService
import com.mifos.core.network.services.SearchService
import com.mifos.core.network.services.ShareAccountService
import com.mifos.core.network.services.StaffService
import com.mifos.core.network.services.StandingInstructionService
import com.mifos.core.network.services.SurveyService
import com.mifos.core.network.services.createCenterService
import com.mifos.core.network.services.createChargeService
import com.mifos.core.network.services.createCheckerInboxService
import com.mifos.core.network.services.createClientAccountsService
import com.mifos.core.network.services.createClientService
import com.mifos.core.network.services.createCollectionSheetService
import com.mifos.core.network.services.createDataTableService
import com.mifos.core.network.services.createDocumentService
import com.mifos.core.network.services.createFixedDepositService
import com.mifos.core.network.services.createGroupService
import com.mifos.core.network.services.createLoanService
import com.mifos.core.network.services.createNoteService
import com.mifos.core.network.services.createOfficeService
import com.mifos.core.network.services.createRecurringAccountService
import com.mifos.core.network.services.createRunReportsService
import com.mifos.core.network.services.createSavingsAccountService
import com.mifos.core.network.services.createSearchService
import com.mifos.core.network.services.createShareAccountService
import com.mifos.core.network.services.createStaffService
import com.mifos.core.network.services.createStandingInstructionService
import com.mifos.core.network.services.createSurveyService
import de.jensklingenberg.ktorfit.Ktorfit

class BaseApiManager(
    private val prefManager: UserPreferencesRepository,
    private val ktorfit: Ktorfit,
) {

    val centerService: CenterService = ktorfit.createCenterService()
    val accountsService: ClientAccountsService = ktorfit.createClientAccountsService()
    val clientService: ClientService = ktorfit.createClientService()
    val dataTableService: DataTableService = ktorfit.createDataTableService()
    val loanService: LoanService = ktorfit.createLoanService()
    val savingsService: SavingsAccountService = ktorfit.createSavingsAccountService()
    val fixedDepositService: FixedDepositService = ktorfit.createFixedDepositService()

    val recurringSavingsAccountService: RecurringAccountService = ktorfit.createRecurringAccountService()
    val searchService: SearchService = ktorfit.createSearchService()
    val groupService: GroupService = ktorfit.createGroupService()
    val documentService: DocumentService = ktorfit.createDocumentService()
    val officeService: OfficeService = ktorfit.createOfficeService()
    val staffService: StaffService = ktorfit.createStaffService()
    val surveyService: SurveyService = ktorfit.createSurveyService()
    val chargeService: ChargeService = ktorfit.createChargeService()
    val checkerInboxService: CheckerInboxService = ktorfit.createCheckerInboxService()
    val collectionSheetService: CollectionSheetService = ktorfit.createCollectionSheetService()
    val noteService: NoteService = ktorfit.createNoteService()
    val runReportsService: RunReportsService = ktorfit.createRunReportsService()
    val shareAccountService: ShareAccountService = ktorfit.createShareAccountService()
    val standingInstructionService: StandingInstructionService = ktorfit.createStandingInstructionService()

    // sdk apis
    val clientIdentifiersApi: ClientIdentifierApi = ktorfit.createClientIdentifierApi()
    val centerApi: CentersApi = ktorfit.createCentersApi()
    val officeApi: OfficesApi = ktorfit.createOfficesApi()
    val clientsApi: ClientApi = ktorfit.createClientApi()
    val groupApi: GroupsApi = ktorfit.createGroupsApi()
    val staffApi: StaffApi = ktorfit.createStaffApi()
    val dataTableApi: DataTablesApi = ktorfit.createDataTablesApi()
    companion object {
        fun build(prefManager: UserPreferencesRepository): BaseApiManager {
            val ktorfitClient = KtorfitClient.builder()
                .httpClient(KtorHttpClient)
                .baseURL(prefManager.getServerConfig.value.getInstanceUrl())
                .build()

            return BaseApiManager(prefManager, ktorfitClient.ktorfit)
        }
    }
}
