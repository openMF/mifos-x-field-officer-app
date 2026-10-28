/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package cmp.navigation.di

import cmp.navigation.ComposeAppViewModel
import cmp.navigation.authenticated.AuthenticatedNavbarNavigationViewModel
import cmp.navigation.rootnav.RootNavViewModel
import com.mifos.core.common.network.di.DispatchersModule
import com.mifos.core.data.di.RepositoryModule
import com.mifos.core.datastore.di.PreferencesModule
import com.mifos.core.domain.di.UseCaseModule
import com.mifos.core.network.di.DataManagerModule
import com.mifos.core.network.di.NetworkModule
import com.mifos.feature.activate.di.ActivateModule
import com.mifos.feature.auth.di.AuthModule
import com.mifos.feature.center.di.CenterModule
import com.mifos.feature.checker.inbox.task.di.CheckerInboxTaskModule
import com.mifos.feature.client.di.ClientModule
import com.mifos.feature.dataTable.di.DataTableModule
import com.mifos.feature.document.di.DocumentModule
import com.mifos.feature.groups.di.GroupsModule
import com.mifos.feature.individualCollectionSheet.di.CollectionSheetModule
import com.mifos.feature.loan.di.LoanModule
import com.mifos.feature.note.di.NoteModule
import com.mifos.feature.offline.di.OfflineModule
import com.mifos.feature.passcode.di.MifosAuthenticatorModule
import com.mifos.feature.path.tracking.di.PathTrackingModule
import com.mifos.feature.recurringDeposit.di.RecurringDepositModule
import com.mifos.feature.report.di.ReportModule
import com.mifos.feature.savings.di.SavingsModule
import com.mifos.feature.search.di.SearchModule
import com.mifos.feature.searchrecord.di.SearchRecordModule
import com.mifos.feature.settings.di.SettingsModule
import com.mifos.feature.standingInstruction.di.StandingInstructionsModule
import com.mifos.room.di.DaoModule
import com.mifos.room.di.HelperModule
import com.mifos.room.di.PlatformSpecificDatabaseModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import template.core.base.common.di.CommonModule

object KoinModules {

    private val commonModules = module { includes(DispatchersModule) }
    private val coreBaseCommonModules = module { includes(CommonModule) }
    private val domainModule = module { includes(UseCaseModule) }
    private val dataModules = module { includes(RepositoryModule) }
    private val coreDataStoreModules = module { includes(PreferencesModule) }
    private val databaseModules = module {
        includes(
            DaoModule,
            HelperModule,
            PlatformSpecificDatabaseModule,
        )
    }

    private val networkModules = module {
        includes(
            DataManagerModule,
            NetworkModule,
        )
    }
    private val sharedModule = module {
        viewModelOf(::ComposeAppViewModel)
        viewModelOf(::RootNavViewModel)
        viewModelOf(::AuthenticatedNavbarNavigationViewModel)
    }

    private val featureModules = module {
        includes(
            ActivateModule,
            AuthModule,
            CenterModule,
            CheckerInboxTaskModule,
            ClientModule,
            CollectionSheetModule,
            DataTableModule,
            GroupsModule,
            DocumentModule,
            LoanModule,
            NoteModule,
            OfflineModule,
            PathTrackingModule,
            ReportModule,
            RecurringDepositModule,
            SavingsModule,
            SearchModule,
            SettingsModule,
            SearchRecordModule,
            MifosAuthenticatorModule,
            StandingInstructionsModule,
        )
    }

    val allModules = listOf(
        sharedModule,
        commonModules,
        domainModule,
        dataModules,
        databaseModules,
        featureModules,
        networkModules,
        coreDataStoreModules,
        coreBaseCommonModules,
    )
}
