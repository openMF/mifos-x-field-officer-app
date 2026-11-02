/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import androidx.navigation.toRoute
import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.composableWithStayTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.common.utils.Constants
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.searchrecord.RecordType
import kpt.feature.activate.navigation.navigateToActivateClient
import kpt.feature.charge.navigation.navigateToAddCharge
import kpt.feature.charge.navigation.navigateToCharges
import kpt.feature.client.addaddress.ClientAddAddressScreen
import kpt.feature.client.adddocument.ClientAddDocumentScreen
import kpt.feature.client.addidentifier.ClientAddIdentifierScreen
import kpt.feature.client.address.ClientAddressScreen
import kpt.feature.client.applynew.ClientApplyNewScreen
import kpt.feature.client.closure.ClientClosureScreen
import kpt.feature.client.collateral.ClientCollateralScreen
import kpt.feature.client.collateraldetails.ClientCollateralDetailsScreen
import kpt.feature.client.createnew.CreateNewClientScreen
import kpt.feature.client.defaultaccount.ClientDefaultAccountScreen
import kpt.feature.client.depositaccounts.ClientDepositAccountsScreen
import kpt.feature.client.depositaccounts.DepositAccountKind
import kpt.feature.client.details.ClientDetailsScreen
import kpt.feature.client.documentpreview.ClientDocumentPreviewScreen
import kpt.feature.client.documentpreview.NO_DOCUMENT_ID
import kpt.feature.client.documents.ClientDocumentsScreen
import kpt.feature.client.editdetails.ClientEditDetailsScreen
import kpt.feature.client.editprofile.ClientEditProfileScreen
import kpt.feature.client.fixeddepositapplication.FixedDepositApplicationScreen
import kpt.feature.client.general.ClientGeneralScreen
import kpt.feature.client.identifiers.ClientIdentifiersScreen
import kpt.feature.client.list.ClientListScreen
import kpt.feature.client.loanaccounts.ClientLoanAccountsScreen
import kpt.feature.client.profile.ClientProfileScreen
import kpt.feature.client.shareaccounts.ClientShareAccountsScreen
import kpt.feature.client.shareapplication.ShareApplicationScreen
import kpt.feature.client.signature.ClientSignatureScreen
import kpt.feature.client.staff.ClientStaffScreen
import kpt.feature.client.transfer.ClientTransferScreen
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.loan.navigation.navigateToLoanProfile
import kpt.feature.loan.navigation.navigateToLoanRepayment
import kpt.feature.loan.navigation.navigateToNewLoanApplication
import kpt.feature.note.navigation.navigateToClientNotes
import kpt.feature.note.navigation.noteDestinations
import kpt.feature.savings.navigation.navigateToRecurringDepositApplication
import kpt.feature.savings.navigation.navigateToSavingsApplication
import kpt.feature.savings.navigation.navigateToSavingsApproval
import kpt.feature.savings.navigation.navigateToSavingsSummary
import kpt.feature.searchrecord.navigation.navigateToSearchRecord
import kpt.feature.searchrecord.navigation.searchRecordDestination

/** The tab's graph. Mirrors `HomeDestination` — the tab points at the graph, not at a screen. */
@Serializable
data object ClientDestination

@Serializable
data object ClientListRoute

/**
 * The client PROFILE — what tapping a client in the list opens.
 *
 * Named for the original's `ClientProfileRoute`, which is what `clientListScreenRoute(onClientSelect
 * = navController::navigateToClientProfileRoute)` pointed at. An earlier pass of this port called it
 * `ClientDetailRoute` and pointed it at a rebuilt screen; the original's `clientDetails` (884 lines)
 * is a SEPARATE, legacy destination the list never opened.
 *
 * `clientId` is a type-safe route argument, not a SavedStateHandle default.
 */
@Serializable
data class ClientProfileRoute(val clientId: Int)

/**
 * Assign a loan officer to a client.
 *
 * `clientId`, not the original's bare `id: Int = -1`. The default existed because the original read
 * the route through `savedStateHandle.toRoute<ClientStaffRoute>()` and needed a value when the key
 * was absent — a screen reachable with id `-1` that would then ask Fineract for client -1. A
 * type-safe route with no default makes the caller supply it.
 */
@Serializable
data class ClientStaffRoute(val clientId: Int)

/** Close a client. Same `clientId`-without-a-default reasoning as [ClientStaffRoute]. */
@Serializable
data class ClientClosureRoute(val clientId: Int)

/** Propose transferring a client to another office. */
@Serializable
data class ClientTransferRoute(val clientId: Int)

/** Pledge collateral against a client. */
@Serializable
data class ClientCollateralRoute(val clientId: Int)

/**
 * The client's GENERAL view — performance history + linked-account actions. Reached from the
 * profile's "General" row, and where the client's accounts actually live.
 */
@Serializable
data class ClientGeneralRoute(val clientId: Int)

/** The client's identity documents. Reached from the profile's "Identifiers" row. */
@Serializable
data class ClientIdentifiersRoute(val clientId: Int)

/**
 * The client's DETAIL fields + its eight actions — what the profile's ProfileCard taps through to.
 *
 * Named for the original's `clientDetailsProfile`. Not to be confused with the original's separate
 * legacy `clientDetails` screen, which nothing navigates to (see [ClientProfileRoute]).
 */
/**
 * "Apply for a new account" — the client-side chooser.
 *
 * `clientId` only. The original's route also carried `status` and `accountNo`; the status is read
 * from the client instead (a nav-arg status is a snapshot that goes stale — see
 * `ClientApplyNewViewModel`).
 */
@Serializable
data class ClientApplyNewRoute(val clientId: Int)

@Serializable
data class ClientDetailsRoute(val clientId: Int)

/** The client's postal addresses. Reached from the profile's "Address" row. */
@Serializable
data class ClientAddressRoute(val clientId: Int)

/**
 * The collateral a client has already PLEDGED — read-only. Reached from client-general's
 * "Collateral Data" row.
 *
 * Distinct from [ClientCollateralRoute], which is the ADD form reached from client details'
 * "Create Collateral". An earlier pass pointed General's row at the form; the original points it
 * here.
 */
@Serializable
data class ClientCollateralDetailsRoute(val clientId: Int)

/**
 * The client's LOAN ACCOUNTS list.
 *
 * In the original this is the only caller of the loan account PROFILE
 * (`clientLoanAccountsDestination(navigateToViewAccount = navigateToLoanAccountProfileScreen)`),
 * which is why it is ported here rather than later: `feature/loan`'s profile had no caller at all
 * until S4d wired search to the loan SUMMARY, and this is its own path in. See the spec's
 * "Verify the PATH into the screen" rule.
 */
@Serializable
data class ClientLoanAccountsRoute(val clientId: Int)

/**
 * A client's deposit accounts of ONE kind — savings, fixed deposit or recurring deposit.
 *
 * [kind] is a [DepositAccountKind] name. The original had three separate routes over three
 * near-identical screens reading the same list; see [ClientDepositAccountsViewModel] for why they
 * collapsed into one parameterised destination.
 */
@Serializable
data class ClientDepositAccountsRoute(val clientId: Int, val kind: String)

/**
 * Create a new client. Takes NO arguments — the form reads the tenant template itself.
 *
 * The original had this screen with no caller at all; the ported client list already carried a
 * null-gated create FAB waiting for it, so the port is what makes it reachable.
 */
@Serializable
data object CreateNewClientRoute

/** Edit a client's details. Same `clientId`-without-a-default reasoning as [ClientStaffRoute]. */
@Serializable
data class ClientEditDetailsRoute(val clientId: Int)

/**
 * The client's profile PHOTO. Reached from client details' "Update Photo".
 *
 * Carries `clientName` + `accountNo` for the same reason [ClientSignatureRoute] does: the screen
 * shows them in its header and has no client read of its own.
 */
@Serializable
data class ClientEditProfileRoute(
    val clientId: Int,
    val clientName: String,
    val accountNo: String,
)

/** Nominate a client's default savings account. Same no-default reasoning as [ClientStaffRoute]. */
@Serializable
data class ClientDefaultAccountRoute(val clientId: Int)

/** Attach a document to a client. Reached from the client documents screen's "+". */
@Serializable
data class ClientAddDocumentRoute(val clientId: Int)

/** Add a postal address to a client. Reached from the client address screen's "+". */
@Serializable
data class ClientAddAddressRoute(val clientId: Int)

/**
 * Add an identity document to a client. Reached from the client identifiers screen's "+".
 *
 * `clientId` is a Long here because the identifier commands and the template store are keyed by
 * Long, unlike every other client route.
 */
@Serializable
data class ClientAddIdentifierRoute(val clientId: Long)

/** A client's SHARE accounts. Reached from client-general's "Share Accounts" row. */
@Serializable
data class ClientShareAccountsRoute(val clientId: Int)

/** Open a SHARE account. Reached from the apply-new chooser and from the share-accounts list. */
@Serializable
data class ShareApplicationRoute(val clientId: Int)

/**
 * Open a FIXED DEPOSIT account. Reached from the apply-new chooser and from the deposit-accounts
 * list when it is showing the FIXED kind.
 */
@Serializable
data class FixedDepositApplicationRoute(val clientId: Int)

/**
 * The client's signature. Reached from client details' "Update Signature".
 *
 * Carries `clientName` + `accountNo` because the screen shows them in its header and has no
 * client read of its own — it reads the DOCUMENT list. The original threaded the same two values
 * through its route for the same reason.
 */
@Serializable
data class ClientSignatureRoute(
    val clientId: Int,
    val clientName: String,
    val accountNo: String,
)

/** The client's attached documents. Reached from the profile's "Documents" row. */
@Serializable
data class ClientDocumentsRoute(val clientId: Int)

/**
 * One saved document, shown in the app. Reached from the documents list's "View" (by
 * [documentId]) and the identifiers list's "View document" (by [matchKey] / [matchPrefix], because
 * an identifier does not know its document's id — see `findIdentifierDocument`).
 */
@Serializable
data class ClientDocumentPreviewRoute(
    val entityType: String,
    val entityId: Int,
    val title: String,
    val documentId: Int = NO_DOCUMENT_ID,
    val matchKey: String = "",
    val matchPrefix: String = "",
)

/**
 * The client list — a TOP-LEVEL destination on the authenticated graph, so it is annotated and
 * collected into `GeneratedFeatureDestinations` by `:cmp-navigation:generateFeatureDestinations`.
 *
 * This file MUST live in a `navigation` package: the generator calls `scan(roots, "navigation")`, so
 * an annotated destination anywhere else is silently skipped and the screen simply does not exist in
 * the app — with a clean build. (A wrong SIGNATURE throws loudly; a wrong PACKAGE does not. Found the
 * hard way in S3a, when `ClientModule` appeared in the Koin aggregate and the destination did not.)
 *
 * The original registered ~38 client destinations through one `clientNavGraph`. Those arrive with
 * their screens across S3a–S3d; nested drill-downs stay nested and are NOT annotated — only
 * top-level entries are (spec §4).
 *
 * Create-client does not exist yet, so it is passed as `null` rather than as an empty lambda — the
 * screen then omits the FAB entirely instead of drawing one that does nothing. S3c supplies it.
 */
@FeatureDestination
fun NavGraphBuilder.clientListDestination(navController: NavController) {
    composableWithStayTransitions<ClientListRoute> {
        ClientListScreen(
            // Detail exists as of S3b, so rows are clickable. The create FAB went live in
            // cluster B; it was null (not an empty lambda) so the FAB was not drawn at all.
            onClientClick = navController::navigateToClientProfile,
            onCreateClient = navController::navigateToCreateNewClient,
        )
    }

    // Nested drill-down: registered inside the feature graph and deliberately NOT annotated. Only
    // TOP-LEVEL entries carry @FeatureDestination (spec §4) — annotating this one would register the
    // profile screen as its own root entry.
    composableWithPushTransitions<ClientProfileRoute> {
        ClientProfileScreen(
            onNavigateBack = navController::popBackStackSafely,
            onNavigateToClientDetails = navController::navigateToClientDetails,
            // Live as of S3b/S5. The rest are null, so their ROWS are not drawn at all rather than
            // drawn inert (RULE-IMPL-DEAD-CLICKABLE-001).
            onNotes = navController::navigateToClientNotes,
            onIdentifiers = navController::navigateToClientIdentifiers,
            onDocuments = navController::navigateToClientDocuments,
            onViewAddress = navController::navigateToClientAddress,
            onViewAssociatedAccounts = navController::navigateToClientGeneral,
            // Blocked on Google Maps — see `client-pinpoint-blocked-on-maps-key`.
            onPinpointLocation = null,
            // The original's `navigateToGroupDetails` — group detail (feature/groups) has landed.
            onNavigateToGroupDetails = navController::navigateToGroupsDetail,
        )
    }

    // Assign staff. Nested drill-down — NOT annotated: only TOP-LEVEL entries carry
    // @FeatureDestination (spec §4), and annotating this would register it as its own root entry.
    composableWithPushTransitions<ClientStaffRoute> {
        ClientStaffScreen(
            onNavigateBack = navController::popBackStackSafely,
            // The original navigated FORWARD to client detail on success, which re-pushed detail on
            // top of the detail the officer came FROM. Popping returns to the live detail screen,
            // which re-reads the client and shows the new officer.
            onNavigateNext = { navController.popBackStackSafely() },
        )
    }

    // Close a client. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientClosureRoute> {
        ClientClosureScreen(
            onNavigateBack = navController::popBackStackSafely,
            // On success the client is CLOSED, so returning to its detail screen is right: the
            // detail re-reads and shows the closed status. The original navigated to the client
            // list instead, which discarded the back stack the officer came through.
            onNavigateNext = { navController.popBackStackSafely() },
        )
    }

    // Propose transfer. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientTransferRoute> {
        ClientTransferScreen(
            onNavigateBack = navController::popBackStackSafely,
            onNavigateNext = { navController.popBackStackSafely() },
        )
    }

    // Add collateral. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientCollateralRoute> {
        ClientCollateralScreen(
            onNavigateBack = navController::popBackStackSafely,
            onNavigateNext = { navController.popBackStackSafely() },
        )
    }

    clientDrillDownDestinations(navController)
    clientAccountDestinations(navController)
    clientFormDestinations(navController)
}

private fun NavGraphBuilder.clientDrillDownDestinations(navController: NavController) {
    // The client's general view. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientGeneralRoute> {
        ClientGeneralScreen(
            onNavigateBack = navController::popBackStackSafely,
            // The original's "Collateral Data" row opens the read-only PLEDGED list, not the add
            // form — `clientProfileGeneralDestination(collateralData =
            // navigateToClientCollateralDetailRoute)`. An earlier pass of this port had it on the
            // form; corrected. The add form is reached from client details' "Create Collateral".
            onCollateralData = navController::navigateToClientCollateralDetails,
            // The ONE charge list (`feature/charge`), scoped to this client — not a port of
            // `clientUpcomingCharges`. That original is a second screen over the SAME endpoint
            // (`getListOfPagingCharges` vs `getListOfClientCharges`, the same path with
            // `?offset=0&limit=0`); neither filters for anything "upcoming" — the name is a
            // misnomer (`upcoming-charges-is-the-same-list-as-charges`).
            onUpcomingCharges = { clientId -> navController.navigateToCharges(ChargeResource.CLIENT, clientId) },
            // The client's loan accounts — and the loan PROFILE's only path in, which is why this
            // row is no longer null-gated. See `ClientLoanAccountsRoute`.
            onLoanAccounts = navController::navigateToClientLoanAccounts,
            // The three deposit rows. `null` (so ABSENT, not drawn dead) until S6-A1 landed the
            // one parameterised destination that replaced the original's three near-identical
            // screens — see `ClientDepositAccountsRoute`.
            onSavingAccounts = {
                navController.navigateToClientDepositAccounts(it, DepositAccountKind.SAVINGS)
            },
            onFixedDepositAccounts = {
                navController.navigateToClientDepositAccounts(it, DepositAccountKind.FIXED)
            },
            onRecurringDepositAccounts = {
                navController.navigateToClientDepositAccounts(it, DepositAccountKind.RECURRING)
            },
            // Live as of cluster D. A separate screen over a different entity — share accounts
            // carry approved/pending COUNTS, not a balance — so it did not fold into the
            // parameterised deposit destination above.
            onSharesAccounts = navController::navigateToClientShareAccounts,
        )
    }

    // The client's identity documents. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientIdentifiersRoute> {
        ClientIdentifiersScreen(
            onBackPress = navController::popBackStackSafely,
            // Live as of cluster C — null (so the "+" was ABSENT, not inert) until the add form
            // landed.
            onAddNewIdentifier = navController::navigateToClientAddIdentifier,
            onNavigateToSearch = { navController.navigateToSearchRecord(RecordType.IDENTIFIER) },
            // The original form's VIEW_DOCUMENT phase, now the shared in-app preview. The scan
            // is a `client_identifiers` document keyed by the CLIENT's id — the original's
            // storage convention, kept so scans it uploaded are still found.
            onViewDocument = { event ->
                navController.navigateToClientDocumentPreview(
                    ClientDocumentPreviewRoute(
                        entityType = Constants.ENTITY_TYPE_CLIENT_IDENTIFIERS,
                        entityId = event.clientId,
                        title = event.title,
                        matchKey = event.matchKey,
                        matchPrefix = event.matchPrefix,
                    ),
                )
            },
        )
    }

    // The client's detail fields + actions. This is what makes the four S3c action forms
    // reachable: Closure, Transfer, Assign Staff and Create Collateral all route from here.
    // Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientApplyNewRoute> {
        ClientApplyNewScreen(
            onBackPressed = navController::popBackStackSafely,
            // Every row is drawn: loan, savings, recurring deposit, share and fixed deposit all
            // have an application now, so the chooser finally offers every product type the
            // client's eligibility allows rather than the subset this port had reached.
            onApplyLoan = navController::navigateToNewLoanApplication,
            // This is what gives the savings wizard its CLIENT path — S5c wired only the group
            // path, from group detail, so `isGroup = false` was unreachable.
            onApplySavings = { clientId ->
                navController.navigateToSavingsApplication(ownerId = clientId, isGroup = false)
            },
            onApplyRecurringDeposit = navController::navigateToRecurringDepositApplication,
            onApplyShare = navController::navigateToShareApplication,
            onApplyFixedDeposit = navController::navigateToFixedDepositApplication,
        )
    }

    composableWithPushTransitions<ClientDetailsRoute> {
        ClientDetailsScreen(
            onNavigateBack = navController::popBackStackSafely,
            onClosureApplication = navController::navigateToClientClosure,
            onTransferClient = navController::navigateToClientTransfer,
            onAssignStaff = navController::navigateToClientStaff,
            onCreateCollateral = navController::navigateToClientCollateral,
            // Straight to the add form — the original's `navigateToChargesRoute(it, clients)`
            // opened on that same form. Was null (row absent) until feature/charge landed a
            // client-charge CREATE; before that it pointed at a read-only list.
            onAddCharge = { clientId -> navController.navigateToAddCharge(ChargeResource.CLIENT, clientId) },
            onApplyNewApplication = navController::navigateToClientApplyNew,
            // Live as of cluster B — each was null (so the row was ABSENT, not inert) until its
            // destination landed.
            onUpdateDefaultAccount = navController::navigateToClientDefaultAccount,
            onUpdateDetails = navController::navigateToClientEditDetails,
            onUpdatePhoto = navController::navigateToClientEditProfile,
            // Standing instructions has NO original at 6b66e8a43 — there is nothing to port, so
            // this row is permanently absent rather than pending.
            onCreateStandingInstructions = null,
            onUpdateSignature = navController::navigateToClientSignature,
            // The original's `activateClient` → feature/activate (was a route with no caller).
            onActivate = navController::navigateToActivateClient,
        )
    }
}

private fun NavGraphBuilder.clientAccountDestinations(navController: NavController) {
    // The client's postal addresses + attached documents. Nested drill-downs, NOT annotated.
    composableWithPushTransitions<ClientAddressRoute> {
        ClientAddressScreen(
            onNavigateBack = navController::popBackStackSafely,
            // The add-address form is a 610-line screen (S3d) — null, so the "+" is absent.
            // Live as of cluster C — null (so the "+" was ABSENT) until the form landed.
            onAddAddress = navController::navigateToClientAddAddress,
            onNavigateToSearch = { navController.navigateToSearchRecord(RecordType.ADDRESS) },
        )
    }

    composableWithPushTransitions<ClientDocumentsRoute> {
        ClientDocumentsScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Live as of cluster C — null (so the "+" was ABSENT, not inert) until the add form
            // landed. The in-place search filter needs no destination and was always wired.
            onAddDocument = navController::navigateToClientAddDocument,
            onViewDocument = { event ->
                navController.navigateToClientDocumentPreview(
                    ClientDocumentPreviewRoute(
                        entityType = Constants.ENTITY_TYPE_CLIENTS,
                        entityId = event.clientId,
                        title = event.title,
                        documentId = event.documentId,
                    ),
                )
            },
        )
    }

    // One saved document, shown in the app. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientDocumentPreviewRoute> {
        ClientDocumentPreviewScreen(onNavigateBack = navController::popBackStackSafely)
    }

    // The client's pledged collateral, read-only. Nested drill-down, NOT annotated.
    // The client's loan accounts. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientLoanAccountsRoute> {
        ClientLoanAccountsScreen(
            onNavigateBack = navController::popBackStackSafely,
            // The loan PROFILE, not the summary — this list is what the original points at the
            // profile, and the two are different screens over the same loan.
            onViewAccount = navController::navigateToLoanProfile,
            onMakeRepayment = navController::navigateToLoanRepayment,
            // `newLoanAccount` is a ~2,900-line five-page wizard and is not ported, so the "+"
            // and the empty state's create button are not drawn at all.
            // The new-application wizard in `feature/loan` — the original's
            // `clientLoanAccountsDestination(createAccount = …)`. This was `null` (so the "add
            // account" affordance was ABSENT rather than drawn dead) until S4l landed the screen.
            onAddAccount = navController::navigateToNewLoanApplication,
        )
    }

    // The client's deposit accounts of one kind. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientDepositAccountsRoute> {
        // The route's `kind` is NOT read here any more.
        //
        // It used to be, because whether the create affordance existed was a navigation fact:
        // savings and recurring deposit had a ported wizard and fixed deposit did not, so FIXED
        // got `null` to keep the "+" ABSENT rather than inert. Since the fixed-deposit wizard
        // landed, all three kinds have a destination and the branch is uniform — the screen reads
        // the kind from its own `SavedStateHandle` for everything else.
        //
        // Worth recording how the stale version was found: an edit updated the INNER branch and
        // silently missed this outer one. Both forms compile, so the only symptom was a missing
        // "+" on an empty fixed-deposit list — caught on device, not by the build.
        ClientDepositAccountsScreen(
            onNavigateBack = navController::popBackStackSafely,
            // The account SUMMARY, addressed by the row's own deposit-type endpoint so a
            // recurring-deposit row opens `recurringdepositaccounts` rather than `savingsaccounts`.
            onViewAccount = { accountId, endpoint ->
                navController.navigateToSavingsSummary(accountId, endpoint)
            },
            // The approval half of the status-change screen. The ACCOUNT id — the original passed
            // the CLIENT id here, which approved the wrong record.
            onApproveAccount = navController::navigateToSavingsApproval,
            // All three kinds have a create destination as of cluster D, so the "+" and the empty
            // state's create button are drawn for every one.
            //
            onAddAccount = { clientId, which ->
                when (which) {
                    // The savings wizard serves both owners; a client is not a group.
                    DepositAccountKind.SAVINGS ->
                        navController.navigateToSavingsApplication(clientId, isGroup = false)
                    DepositAccountKind.RECURRING ->
                        navController.navigateToRecurringDepositApplication(clientId)
                    DepositAccountKind.FIXED ->
                        navController.navigateToFixedDepositApplication(clientId)
                }
            },
        )
    }
}

private fun NavGraphBuilder.clientFormDestinations(navController: NavController) {
    // Create a new client. Reached from the list's FAB. Nested drill-down, NOT annotated.
    composableWithPushTransitions<CreateNewClientRoute> {
        CreateNewClientScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Open the client that was just created — the original's behaviour.
            onClientCreated = navController::navigateToClientProfile,
        )
    }

    // Edit client details. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientEditDetailsRoute> {
        ClientEditDetailsScreen(
            onNavigateBack = navController::popBackStackSafely,
            // POP on success rather than navigating forward. The original pushed a client detail
            // on top of the detail the officer came FROM; popping returns to the live screen,
            // which re-reads and shows the edit.
            onNavigateNext = navController::popBackStackSafely,
        )
    }

    // The client's profile photo. Nested drill-down, NOT annotated (same as signature).
    composableWithPushTransitions<ClientEditProfileRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ClientEditProfileRoute>()
        ClientEditProfileScreen(
            clientName = route.clientName,
            accountNo = route.accountNo,
            onNavigateBack = navController::popBackStackSafely,
        )
    }

    // Open a fixed deposit account. Nested drill-down, NOT annotated.
    composableWithPushTransitions<FixedDepositApplicationRoute> {
        FixedDepositApplicationScreen(
            onNavigateBack = navController::popBackStackSafely,
            onCreated = navController::popBackStackSafely,
        )
    }

    // Open a share account. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ShareApplicationRoute> {
        ShareApplicationScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Pop on success — the list or chooser behind re-reads.
            onCreated = navController::popBackStackSafely,
        )
    }

    // A client's share accounts. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientShareAccountsRoute> {
        ClientShareAccountsScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Live as of cluster D — the share wizard landed, so the "+" and the empty state's
            // create button are drawn. This is the original's own edge
            // (`ShareAccountsRoute` -> `navigateToCreateShareAccountRoute`).
            onAddAccount = navController::navigateToShareApplication,
        )
    }

    // Add an identity document to a client. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientAddIdentifierRoute> {
        ClientAddIdentifierScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Pop whether a document was attached or skipped — the identifiers list behind
            // re-reads and shows the new row either way.
            onFinished = navController::popBackStackSafely,
        )
    }

    // Add a postal address to a client. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientAddAddressRoute> {
        ClientAddAddressScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Pop on success: the address list behind re-reads and shows the new row.
            onSaved = navController::popBackStackSafely,
        )
    }

    // Attach a document to a client. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientAddDocumentRoute> {
        ClientAddDocumentScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Pop on success: the documents list behind re-reads and shows the new row.
            onUploaded = navController::popBackStackSafely,
        )
    }

    // Nominate the client's default savings account. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientDefaultAccountRoute> {
        ClientDefaultAccountScreen(
            onNavigateBack = navController::popBackStackSafely,
            // Pop, for the same reason edit-details does.
            onNavigateNext = navController::popBackStackSafely,
        )
    }

    composableWithPushTransitions<ClientCollateralDetailsRoute> {
        ClientCollateralDetailsScreen(
            onNavigateBack = navController::popBackStackSafely,
        )
    }

    // The client's signature. Nested drill-down, NOT annotated.
    composableWithPushTransitions<ClientSignatureRoute> { entry ->
        val route = entry.toRoute<ClientSignatureRoute>()
        ClientSignatureScreen(
            clientName = route.clientName,
            accountNo = route.accountNo,
            onNavigateBack = navController::popBackStackSafely,
        )
    }

    // Notes list + add/edit, owned by `feature/note` and registered here.
    //
    // The ORIGINAL did exactly this: `noteDestination(navController, onBackPressed)` called from
    // the client graph, with `feature/client` depending on `projects.feature.note`. Keeping the
    // dependency direction is what makes the remaining drill-down slices mechanical — client is
    // the hub, and each feature owns its own routes and screens.
    //
    // Nested, so NOT annotated: only TOP-LEVEL entries carry @FeatureDestination (spec §4).
    noteDestinations(navController)

    // Record search, owned by `feature/searchrecord`. The ORIGINAL registered it in the app shell
    // (`AuthenticatedNavbarNavigationScreen`); it belongs here instead because both ends are client
    // screens — it is opened from the address / identifiers headers, and a result opens that
    // client's address or identifier list. Same routing as the original, by the record's type.
    searchRecordDestination(
        onBackClick = navController::popBackStackSafely,
        onRecordSelected = { record ->
            val clientId = record.metadata[Constants.CLIENT_ID]?.toIntOrNull()
            if (clientId == null) {
                Logger.w(TAG) { "search result ${record.type}/${record.id} has no client id; not navigating" }
            } else if (record.type.equals(RecordType.ADDRESS.name, ignoreCase = true)) {
                navController.navigateToClientAddress(clientId)
            } else if (record.type.equals(RecordType.IDENTIFIER.name, ignoreCase = true)) {
                navController.navigateToClientIdentifiers(clientId)
            }
        },
    )
}

/**
 * The client graph for the bottom-nav tab's INNER NavHost.
 *
 * Two NavHosts are in play and they want different things. [clientListDestination] registers the list
 * on the OUTER authenticated graph, where another feature can push it full-screen. This graph is what
 * the Clients TAB renders: the list is the tab's start destination, so the bottom bar stays visible and
 * the tab keeps its own back stack, while the detail drill-down pushes on the OUTER controller and
 * covers the bar — the standard affordance, and the shape `ForkTabRegistry.extraInlineTabDestinations`
 * documents.
 *
 * Not annotated: `@FeatureDestination` collects entries for the outer graph, and registering the same
 * routes there twice is a duplicate-destination crash at NavHost build.
 */
fun NavGraphBuilder.clientTabGraph(outerNav: NavController) {
    navigation<ClientDestination>(startDestination = ClientListRoute) {
        composableWithStayTransitions<ClientListRoute> {
            ClientListScreen(
                onClientClick = outerNav::navigateToClientProfile,
                // The TAB-graph copy of the list — the path a field officer actually takes from
                // the bottom bar. Wiring only the other call site would ship a FAB that exists on
                // one entry path and is missing on the one people use.
                onCreateClient = outerNav::navigateToCreateNewClient,
            )
        }
    }
}

private const val TAG = "ClientNavigation"
