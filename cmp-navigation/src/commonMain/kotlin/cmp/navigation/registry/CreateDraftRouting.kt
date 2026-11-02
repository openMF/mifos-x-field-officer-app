/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.draft_form_address
import cmp.navigation.generated.resources.draft_form_center
import cmp.navigation.generated.resources.draft_form_client
import cmp.navigation.generated.resources.draft_form_collateral
import cmp.navigation.generated.resources.draft_form_default_account
import cmp.navigation.generated.resources.draft_form_fixed_deposit
import cmp.navigation.generated.resources.draft_form_group
import cmp.navigation.generated.resources.draft_form_group_loan
import cmp.navigation.generated.resources.draft_form_loan
import cmp.navigation.generated.resources.draft_form_recurring_deposit
import cmp.navigation.generated.resources.draft_form_savings
import cmp.navigation.generated.resources.draft_form_share
import kpt.core.base.store.infra.DraftRecord
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.model.draft.CreateForm
import kpt.feature.center.navigation.navigateToCreateCenter
import kpt.feature.client.navigation.navigateToClientAddAddress
import kpt.feature.client.navigation.navigateToClientCollateral
import kpt.feature.client.navigation.navigateToClientDefaultAccount
import kpt.feature.client.navigation.navigateToCreateNewClient
import kpt.feature.client.navigation.navigateToFixedDepositApplication
import kpt.feature.client.navigation.navigateToShareApplication
import kpt.feature.groups.navigation.navigateToCreateGroup
import kpt.feature.loan.navigation.navigateToGroupLoanApplication
import kpt.feature.loan.navigation.navigateToNewLoanApplication
import kpt.feature.savings.navigation.navigateToRecurringDepositApplication
import kpt.feature.savings.navigation.navigateToSavingsApplication
import kpt.feature.settings.DraftRowExtras
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * The fork half of Sync & Drafts: each create draft is named ("Loan application" · "FOTest Asha")
 * and gets an Open action that returns to its form, where the form's banner offers Resume.
 *
 * Here because only the app module sees every feature's routes; Sync & Drafts itself (template)
 * knows nothing of loans or clients. Non-create drafts (notes) get null and keep the default row.
 */
@Composable
internal fun rememberCreateDraftExtras(navController: NavController): (DraftRecord) -> DraftRowExtras? {
    val repository = koinInject<FormDraftRepository>()
    val drafts by remember(repository) { repository.observeAll() }.collectAsState(emptyList())
    val names = CreateForm.entries.associateWith { stringResource(it.label) }
    val byId = drafts.associateBy { it.id }
    return { record ->
        byId[record.id]?.let { saved ->
            val form = CreateForm.fromKey(saved.draft.form) ?: return@let null
            val name = names.getValue(form)
            DraftRowExtras(
                title = saved.draft.title.ifBlank { name },
                subtitle = name,
                onOpen = { navController.openCreateDraft(form, saved) },
            )
        }
    }
}

/** The form [saved] came from, for the same owner — its banner then offers the draft to resume. */
internal fun NavController.openCreateDraft(form: CreateForm, saved: SavedFormDraft) {
    when (form) {
        CreateForm.CLIENT -> navigateToCreateNewClient()
        CreateForm.GROUP -> navigateToCreateGroup()
        CreateForm.CENTER -> navigateToCreateCenter()
        else -> saved.draft.ownerId?.toInt()?.let { openOwnedDraft(form, it, saved) }
    }
}

/** The forms that hang off an existing owner (client or group) — [owner] is its id. */
private fun NavController.openOwnedDraft(form: CreateForm, owner: Int, saved: SavedFormDraft) {
    when (form) {
        CreateForm.LOAN -> navigateToNewLoanApplication(owner)
        CreateForm.GROUP_LOAN -> navigateToGroupLoanApplication(owner)
        CreateForm.SAVINGS ->
            navigateToSavingsApplication(owner, isGroup = saved.draft.ownerKind == "group")
        CreateForm.ADDRESS -> navigateToClientAddAddress(owner)
        CreateForm.FIXED_DEPOSIT -> navigateToFixedDepositApplication(owner)
        CreateForm.RECURRING_DEPOSIT -> navigateToRecurringDepositApplication(owner)
        CreateForm.SHARE -> navigateToShareApplication(owner)
        CreateForm.COLLATERAL -> navigateToClientCollateral(owner)
        CreateForm.DEFAULT_ACCOUNT -> navigateToClientDefaultAccount(owner)
        CreateForm.CLIENT, CreateForm.GROUP, CreateForm.CENTER -> Unit
    }
}

private val CreateForm.label: StringResource get() = when (this) {
    CreateForm.CLIENT -> Res.string.draft_form_client
    CreateForm.GROUP -> Res.string.draft_form_group
    CreateForm.CENTER -> Res.string.draft_form_center
    CreateForm.LOAN -> Res.string.draft_form_loan
    CreateForm.GROUP_LOAN -> Res.string.draft_form_group_loan
    CreateForm.SAVINGS -> Res.string.draft_form_savings
    CreateForm.ADDRESS -> Res.string.draft_form_address
    CreateForm.FIXED_DEPOSIT -> Res.string.draft_form_fixed_deposit
    CreateForm.RECURRING_DEPOSIT -> Res.string.draft_form_recurring_deposit
    CreateForm.SHARE -> Res.string.draft_form_share
    CreateForm.COLLATERAL -> Res.string.draft_form_collateral
    CreateForm.DEFAULT_ACCOUNT -> Res.string.draft_form_default_account
}
