/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.identifiers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.ui.components.Actions
import kpt.core.ui.components.AppActionsIdentifierListingComponent
import kpt.core.ui.components.AppAlertDialog
import kpt.core.ui.components.AppEmptyCard
import kpt.core.ui.fieldofficer.utils.getClientIdentifierStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_add_record
import kpt.feature.client.generated.resources.feature_client_cancel
import kpt.feature.client.generated.resources.feature_client_delete_dialog_identifier_message
import kpt.feature.client.generated.resources.feature_client_delete_dialog_title
import kpt.feature.client.generated.resources.feature_client_identifier_delete_failed
import kpt.feature.client.generated.resources.feature_client_identifier_delete_offline
import kpt.feature.client.generated.resources.feature_client_identifiers_empty
import kpt.feature.client.generated.resources.feature_client_identifiers_item
import kpt.feature.client.generated.resources.feature_client_identifiers_not_available
import kpt.feature.client.generated.resources.feature_client_identifiers_title
import kpt.feature.client.generated.resources.feature_client_remove
import kpt.feature.client.generated.resources.feature_client_screen_identifiers
import kpt.feature.client.generated.resources.feature_client_search_records_cd
import kpt.feature.client.generated.resources.search
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The client's identity documents — ported from
 * `6b66e8a43:feature/client/clientIdentifiersList/ClientIdentifiersListScreen.kt`.
 *
 * [ClientIdentifiersViewModel] landed in S3b with six tests and **no screen**, so it had no
 * consumer and was not even registered in Koin — dead code that compiled. This is the missing half.
 *
 * Header (title + "N item" + add icon), `AppEmptyCard` when empty, and one
 * [AppActionsIdentifierListingComponent] per identifier with its expanding action menu — the
 * original's, including the `reversed()` ordering. The original's composite
 * `uniqueKeyForHandleDocument` is built by `identifierDocumentKey`, shared with the add form.
 *
 * ## §6a's three adaptations
 *
 * 1. **State reads** — the original's five dialog states (Loading / Error / NoInternet /
 *    DeletedSuccessfully / DeleteConfirmation) collapse to [ScreenState] plus the ONE that is
 *    genuinely a dialog. See the ViewModel's header.
 * 2. **Action dispatch** — unchanged; already `trySendAction`.
 * 3. **Unported destinations** — `UploadAgain` has no destination yet, so it is omitted from
 *    `menuList` rather than listed and ignored. The original's `onActionClicked` had an
 *    `else -> {}` that swallowed unwired entries; a menu entry that does nothing is the dead
 *    control that rule exists to prevent. `ViewDocument` is live (2026-10-23).
 *
 * The header's SEARCH icon is back (2026-10-24): it opens feature/searchrecord, which the client
 * graph registers — the original routed it through the app shell, which a feature cannot reach.
 */
@Composable
internal fun ClientIdentifiersScreen(
    onBackPress: () -> Unit,
    modifier: Modifier = Modifier,
    onAddNewIdentifier: ((Long) -> Unit)? = null,
    onViewDocument: ((ClientIdentifiersEvent.ViewDocument) -> Unit)? = null,
    /** Identifier search. Null draws no search icon — an icon with nowhere to go is a dead control. */
    onNavigateToSearch: (() -> Unit)? = null,
    viewModel: ClientIdentifiersViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val identifiers by viewModel.identifiers.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientIdentifiersEvent.NavigateBack -> onBackPress()
            is ClientIdentifiersEvent.AddIdentifier -> onAddNewIdentifier?.invoke(event.clientId)
            is ClientIdentifiersEvent.ViewDocument -> onViewDocument?.invoke(event)
            ClientIdentifiersEvent.NavigateToSearch -> onNavigateToSearch?.invoke()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_identifiers),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientIdentifiersAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientIdentifiersAction.NavigateBack) },
        // The add affordance is a FAB, not the original's header icon — operator-directed UI
        // change (2026-10-22), recorded as a §6a deviation. See ClientDepositAccountsScreen for
        // the full reasoning; in short, the original's header "+" read as "no way to add here" on
        // a real device, and it passed `contentDescription = null` so it was unlabelled too.
        floatingActionButtonContent = onAddNewIdentifier?.let {
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientIdentifiersAction.AddIdentifierClicked) },
                contentColor = KptTheme.colorScheme.onPrimaryContainer,
                content = {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = stringResource(Res.string.feature_client_add_record),
                    )
                },
            )
        },
    ) { scaffoldPadding ->
        ClientIdentifiersContent(
            state = state,
            identifiers = identifiers,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
            canViewDocument = onViewDocument != null,
            canSearch = onNavigateToSearch != null,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientIdentifiersContent(
    state: ClientIdentifiersState,
    identifiers: ScreenState<List<Identifier>>,
    onAction: (ClientIdentifiersAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Insets from the host [AppScaffold]'s top bar.
     *
     * Defaulted so tests and previews can render this half alone — the production caller always
     * passes the scaffold's own padding. These screens had NO top bar at all before S5d: the shell
     * draws one only for tab roots, so every client push destination rendered with no title and no
     * back affordance (which is why their `NavigateBack` action had no dispatcher).
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
    canViewDocument: Boolean = false,
    /** Whether the header offers search — see [ClientIdentifiersScreen]'s `onNavigateToSearch`. */
    canSearch: Boolean = false,
) {
    // The host scaffold's insets, folded into the modifier this half already receives so
    // the screen's own root layout places them.
    @Suppress("NAME_SHADOWING")
    val modifier = modifier.padding(scaffoldPadding)

    val notAvailable = stringResource(Res.string.feature_client_identifiers_not_available)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md)
            .testTag(ClientIdentifiersTestTags.SCREEN),
    ) {
        Header(
            // Derived from the SAME state the list renders, so the count cannot disagree with it,
            // and it is 0 until content arrives rather than a stale count from a previous client.
            totalItem = ((identifiers as? ScreenState.Content)?.data?.size ?: 0).toString(),
            onAction = onAction,
            canSearch = canSearch,
        )

        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        ScreenContent(
            state = identifiers,
            onRetry = { onAction(ClientIdentifiersAction.Retry) },
            modifier = Modifier.fillMaxSize().testTag(ClientIdentifiersTestTags.LIST),
        ) { list, _ ->
            if (list.isEmpty()) {
                AppEmptyCard(
                    msg = stringResource(Res.string.feature_client_identifiers_empty),
                    modifier = Modifier.testTag(ClientIdentifiersTestTags.EMPTY),
                )
                return@ScreenContent
            }

            LazyColumn {
                // `reversed()` and the single `item {}` wrapper are the original's. The wrapper
                // means the whole list is one lazy item — so it does not actually lazy-load, which
                // is a UI defect kept under §6a and recorded in CAPABILITY_GAPS rather than fixed.
                item {
                    list.reversed().forEachIndexed { index, identifier ->
                        val status = getClientIdentifierStatus(identifier.status)

                        AppActionsIdentifierListingComponent(
                            type = identifier.documentType?.name ?: notAvailable,
                            id = identifier.id?.toString() ?: notAvailable,
                            key = identifier.documentKey ?: notAvailable,
                            status = status,
                            description = identifier.description ?: notAvailable,
                            // The original's own TODO: "check what is identifyDocuments, couldnot
                            // find in the api" — it passed the type again. Kept.
                            identifyDocuments = identifier.documentType?.name ?: notAvailable,
                            // UploadAgain stays absent — see the ViewModel's header. View is
                            // listed only when the host wired the preview destination.
                            menuList = listOfNotNull(
                                Actions.ViewDocument().takeIf { canViewDocument },
                                Actions.DeleteDocument(),
                            ),
                            onActionClicked = { action ->
                                when (action) {
                                    is Actions.ViewDocument ->
                                        onAction(ClientIdentifiersAction.ViewDocument(identifier))

                                    is Actions.DeleteDocument -> identifier.id?.let {
                                        onAction(ClientIdentifiersAction.ShowDeleteConfirmation(it.toLong()))
                                    }

                                    else -> Unit
                                }
                            },
                            onClick = { onAction(ClientIdentifiersAction.ToggleShowMenu(index)) },
                            isExpanded = index == state.currentExpandedItem && state.expandIdentity,
                        )

                        Spacer(Modifier.height(KptTheme.spacing.sm))
                    }
                }
            }
        }
    }

    Dialogs(state = state, onAction = onAction)
}

@Composable
private fun Header(
    totalItem: String,
    onAction: (ClientIdentifiersAction) -> Unit,
    canSearch: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(Res.string.feature_client_identifiers_title),
                style = AppTypography.titleMedium,
            )
            Text(
                text = totalItem + " " + stringResource(Res.string.feature_client_identifiers_item),
                style = AppTypography.labelMedium,
                modifier = Modifier.testTag(ClientIdentifiersTestTags.COUNT),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // The original's search icon, with a description (it passed "") and only when wired.
        if (canSearch) {
            IconButton(
                onClick = { onAction(ClientIdentifiersAction.NavigateToSearch) },
                modifier = Modifier.testTag(ClientIdentifiersTestTags.SEARCH),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.search),
                    contentDescription = stringResource(Res.string.feature_client_search_records_cd),
                )
            }
        }
    }
}

@Composable
private fun Dialogs(
    state: ClientIdentifiersState,
    onAction: (ClientIdentifiersAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientIdentifiersState.DialogState.DeleteConfirmation -> AppAlertDialog(
            dialogTitle = stringResource(Res.string.feature_client_delete_dialog_title),
            dialogText = stringResource(
                Res.string.feature_client_delete_dialog_identifier_message,
                dialog.identifierId.toString(),
            ),
            onDismissRequest = { onAction(ClientIdentifiersAction.CloseDialog) },
            onConfirmation = { onAction(ClientIdentifiersAction.ConfirmDelete(dialog.identifierId)) },
            confirmationText = stringResource(Res.string.feature_client_remove),
            dismissText = stringResource(Res.string.feature_client_cancel),
            modifier = Modifier.testTag(ClientIdentifiersTestTags.DELETE_DIALOG),
        )
    }

    // A failed WRITE. The original folded this into the same dialog family as the read's errors;
    // here the read's states are the content area's and only the write reports through a dialog.
    state.writeError?.let { error ->
        val message = when (error) {
            IdentifierWriteError.Offline -> stringResource(Res.string.feature_client_identifier_delete_offline)
            IdentifierWriteError.Failed -> stringResource(Res.string.feature_client_identifier_delete_failed)
        }
        AppAlertDialog(
            dialogText = message,
            onDismissRequest = { onAction(ClientIdentifiersAction.CloseDialog) },
            onConfirmation = { onAction(ClientIdentifiersAction.CloseDialog) },
            dismissText = null,
            modifier = Modifier.testTag(ClientIdentifiersTestTags.WRITE_ERROR_DIALOG),
        )
    }
}
