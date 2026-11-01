/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.client.Client
import kpt.core.ui.components.AppRowCard
import kpt.core.ui.util.TextUtil
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_group_na
import kpt.feature.client.generated.resources.feature_client_name_na
import kpt.feature.client.generated.resources.feature_client_office_na
import kpt.feature.client.generated.resources.feature_client_profile_actions
import kpt.feature.client.generated.resources.feature_client_profile_profile
import kpt.feature.client.generated.resources.feature_client_screen_profile
import kpt.feature.client.generated.resources.feature_client_string_not_available
import kpt.feature.client.profile.components.ClientProfileActionItem
import kpt.feature.client.profile.components.ProfileCard
import kpt.feature.client.profile.components.clientsActionItems
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The client PROFILE screen — ported from
 * `6b66e8a43:feature/client/clientProfile/ClientProfileScreen.kt`.
 *
 * Layout, order, labels, components and spacing are the original's: a "Profile" label, the
 * primary-coloured [ProfileCard], an "Actions" label, then one `AppRowCard` per entry of
 * [clientsActionItems]. The N/A fallback strings are the original's four.
 *
 * ## It replaces a rebuild
 *
 * An earlier pass shipped a Scaffold + TopAppBar + raw-`Text` identity + hand-rolled
 * "Charges"/"Notes" rows + an accounts section. None of that is in the original, and accounts are
 * not on this screen at all — they are behind the General action (`clientGeneral`). See
 * [ClientProfileViewModel]'s header.
 *
 * ## §6a's three adaptations, and nothing else
 *
 * 1. **State reads** — `DialogState.Loading`/`.Error`, which the original rendered as a dialog over
 *    the whole screen, become [ScreenState] through [ScreenContent]. It supplies retry, which the
 *    original's error dialog also did.
 * 2. **Action dispatch** — unchanged in shape; already `trySendAction`. The EVENTS now carry the
 *    `clientId` rather than the screen reading `state.client?.id ?: -1` at the call site — that
 *    read is why every one of the original's seven branches could navigate to client `-1` while a
 *    load was still in flight.
 * 3. **Unported destinations** — `null` callbacks, so the row is absent rather than inert.
 *    Address, Documents and General are not ported yet; Identifiers, Notes and Pinpoint route (or,
 *    for Pinpoint, are absent — it is blocked on Google Maps).
 *
 * `AppBreadcrumbNavBar(navController)` is dropped, as on every ported screen: it reads the live
 * back stack, so it cannot be rendered by the stateless half or in a test without a NavHost.
 */
@Composable
internal fun ClientProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToClientDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onNotes: ((Int) -> Unit)? = null,
    onDocuments: ((Int) -> Unit)? = null,
    onIdentifiers: ((Int) -> Unit)? = null,
    onViewAddress: ((Int) -> Unit)? = null,
    onViewAssociatedAccounts: ((Int) -> Unit)? = null,
    onPinpointLocation: ((Int) -> Unit)? = null,
    onNavigateToGroupDetails: ((Int) -> Unit)? = null,
    viewModel: ClientProfileViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val client by viewModel.client.collectAsStateWithLifecycle()
    // The wired destination of each action row. FamilyMembers has none: the original's branch is an
    // empty `{}` and the row is not in the list.
    val actionHandlers = mapOf<ClientProfileActionItem, ((Int) -> Unit)?>(
        ClientProfileActionItem.General to onViewAssociatedAccounts,
        ClientProfileActionItem.Address to onViewAddress,
        ClientProfileActionItem.Identifiers to onIdentifiers,
        ClientProfileActionItem.Documents to onDocuments,
        ClientProfileActionItem.Notes to onNotes,
        ClientProfileActionItem.PinpointLocation to onPinpointLocation,
    )

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientProfileEvent.NavigateBack -> onNavigateBack()

            is ClientProfileEvent.NavigateToClientDetailsScreen ->
                onNavigateToClientDetails(event.clientId)

            is ClientProfileEvent.NavigateToGroupDetails ->
                onNavigateToGroupDetails?.invoke(event.groupId)

            is ClientProfileEvent.OnActionClick -> actionHandlers[event.action]?.invoke(event.clientId)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_profile),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientProfileAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientProfileAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientProfileContent(
            state = state,
            client = client,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            // A row is drawn only when its destination exists — absent, never inert
            // (RULE-IMPL-DEAD-CLICKABLE-001).
            availableActions = remember(actionHandlers) {
                clientsActionItems.filter { actionHandlers[it] != null }
            },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin or a signed-in session. */
@Composable
internal fun ClientProfileContent(
    state: ClientProfileState,
    client: ScreenState<Client>,
    onAction: (ClientProfileAction) -> Unit,
    availableActions: List<ClientProfileActionItem>,
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
) {
    // The host scaffold's insets, folded into the modifier this half already receives so
    // the screen's own root layout places them.
    @Suppress("NAME_SHADOWING")
    val modifier = modifier.padding(scaffoldPadding)

    ScreenContent(
        state = client,
        onRetry = { onAction(ClientProfileAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientProfileTestTags.SCREEN),
    ) { clientEntity, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.feature_client_profile_profile),
                style = AppTypography.labelLargeEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.medium))

            ProfileCard(
                image = state.profileImage,
                name = clientEntity.displayName ?: stringResource(Res.string.feature_client_name_na),
                accountNo = clientEntity.accountNo
                    ?: stringResource(Res.string.feature_client_string_not_available),
                office = clientEntity.officeName ?: stringResource(Res.string.feature_client_office_na),
                // Fineract names the client's group in `groups`, never in the scalar `groupName` /
                // `groupId` — so every member read "Group: N/A" (device, 2026-10-26), and `groupId`'s
                // default of 0 would have opened group 0. The scalars stay as the fallback.
                groupName = (clientEntity.groups?.firstOrNull()?.name ?: clientEntity.groupName)
                    ?: stringResource(Res.string.feature_client_group_na),
                onGroupClick = {
                    (clientEntity.groups?.firstOrNull()?.id ?: clientEntity.groupId)
                        ?.takeIf { it > 0 }
                        ?.let { onAction(ClientProfileAction.OnGroupClick(it)) }
                },
                onClick = { onAction(ClientProfileAction.NavigateToClientDetailsScreen) },
                modifier = Modifier.testTag(ClientProfileTestTags.PROFILE_CARD),
            )

            Spacer(Modifier.height(KptTheme.spacing.md))
            Text(
                text = stringResource(Res.string.feature_client_profile_actions),
                style = AppTypography.labelLargeEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.medium))

            availableActions.forEach { item ->
                AppRowCard(
                    title = stringResource(item.title),
                    imageVector = item.icon,
                    leftValues = listOf(
                        TextUtil(
                            text = stringResource(item.subTitle),
                            style = AppTypography.bodySmall,
                            color = KptTheme.colorScheme.secondary,
                        ),
                    ),
                    rightValues = emptyList(),
                    modifier = Modifier
                        .clickable { onAction(ClientProfileAction.OnActionClick(item)) }
                        .padding(vertical = DesignToken.padding.medium)
                        .testTag(ClientProfileTestTags.actionRow(item)),
                )
            }
        }
    }
}
