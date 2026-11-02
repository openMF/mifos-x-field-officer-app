/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.component.KptShimmerLoadingBox
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.feature.profile.TestTags
import kpt.feature.profile.generated.resources.Res
import kpt.feature.profile.generated.resources.feature_profile_change_password
import kpt.feature.profile.generated.resources.feature_profile_change_password_offline
import kpt.feature.profile.generated.resources.feature_profile_live
import kpt.feature.profile.generated.resources.feature_profile_loan_officer
import kpt.feature.profile.generated.resources.feature_profile_name_error
import kpt.feature.profile.generated.resources.feature_profile_name_error_generic
import kpt.feature.profile.generated.resources.feature_profile_office_label
import kpt.feature.profile.generated.resources.feature_profile_offline_banner
import kpt.feature.profile.generated.resources.feature_profile_password_changed
import kpt.feature.profile.generated.resources.feature_profile_retry
import kpt.feature.profile.generated.resources.feature_profile_roles_empty
import kpt.feature.profile.generated.resources.feature_profile_roles_title
import kpt.feature.profile.generated.resources.feature_profile_scope_my_portfolio
import kpt.feature.profile.generated.resources.feature_profile_scope_note
import kpt.feature.profile.generated.resources.feature_profile_scope_office
import kpt.feature.profile.generated.resources.feature_profile_scope_title
import kpt.feature.profile.generated.resources.feature_profile_server_label
import kpt.feature.profile.generated.resources.feature_profile_server_settings
import kpt.feature.profile.generated.resources.feature_profile_session_title
import kpt.feature.profile.generated.resources.feature_profile_sign_out
import kpt.feature.profile.generated.resources.feature_profile_staff
import kpt.feature.profile.generated.resources.feature_profile_tenant
import kpt.feature.profile.generated.resources.feature_profile_title
import kpt.feature.settings.signout.SignOutAction
import kpt.feature.settings.signout.SignOutDialog
import kpt.feature.settings.signout.SignOutViewModel
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The officer's own account (screens/profile, Stitch 01–05). Supplied through
 * `ForkBackboneRegistry.profileBody`, which only wires the navigation. Sign-out is the SAME dialog and
 * ViewModel as Settings' (feature/settings `signout/`), owned here: clearing the session flips
 * `UserData.isAuthenticated` and RootNavViewModel routes to sign-in, so it needs no callback.
 */
@Composable
fun ProfileBody(
    onBack: () -> Unit,
    onServerSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
    signOutViewModel: SignOutViewModel = koinViewModel(),
) {
    val state by viewModel.profile.collectAsStateWithLifecycle()
    val signOut by signOutViewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ProfileEvent.PasswordChanged ->
                snackbar.showSnackbar(getString(Res.string.feature_profile_password_changed))
        }
    }
    ProfileBodyContent(
        state = state,
        onAction = viewModel::trySendAction,
        onBack = onBack,
        onServerSettings = onServerSettings,
        onSignOut = { signOutViewModel.trySendAction(SignOutAction.Requested) },
        snackbarHostState = snackbar,
        modifier = modifier,
    )
    if (signOut.confirming) {
        SignOutDialog(state = signOut, onAction = signOutViewModel::trySendAction)
    }
}

/** Stateless half — previews and UI tests drive it directly. */
@Composable
internal fun ProfileBodyContent(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBack: () -> Unit,
    onServerSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    AppScaffold(
        title = stringResource(Res.string.feature_profile_title),
        onBackPressed = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier.testTag(TestTags.Profile.BODY),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md)
                .padding(bottom = KptTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.lg),
        ) {
            if (!state.online) OfflineBanner()
            Identity(state)
            (state.name as? NameState.Failed)?.let { NameError(it.reason) { onAction(ProfileAction.Retry) } }
            OfficeAndScope(state)
            Roles(state)
            Session(state, onServerSettings)
            Account(state, onChangePassword = { onAction(ProfileAction.OpenChangePassword) }, onSignOut = onSignOut)
        }
    }
    state.password?.let { form -> PasswordSheet(form, onAction) }
}

@Composable
private fun OfflineBanner() {
    val c = KptTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.tertiaryContainer, KptTheme.shapes.medium)
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm + KptTheme.spacing.xs)
            .testTag(TestTags.Profile.OFFLINE_BANNER),
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = c.onTertiaryContainer)
        Text(
            stringResource(Res.string.feature_profile_offline_banner),
            style = KptTheme.typography.bodyMedium,
            color = c.onTertiaryContainer,
        )
    }
}

/**
 * Stitch 02: the one bold element — the initials disc (same initials + tint as the Home avatar) and
 * the name.
 */
@Composable
private fun Identity(state: ProfileUiState) {
    val c = KptTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = KptTheme.spacing.sm).testTag(TestTags.Profile.IDENTITY),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(KptTheme.spacing.xxl + KptTheme.spacing.sm)
                .border(KptTheme.spacing.xs, c.surface, CircleShape)
                .background(c.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                state.initials,
                style = KptTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = c.primary,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
            when {
                state.fullName != null -> Text(
                    state.fullName.toNameCase(),
                    style = KptTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag(TestTags.Profile.FULL_NAME),
                )
                state.name == NameState.Loading -> KptShimmerLoadingBox(
                    Modifier.fillMaxWidth(0.7f).heightIn(min = KptTheme.spacing.lg + KptTheme.spacing.xs),
                )
                // No name from the server: the username stands in as the title.
                else -> Text(
                    state.username,
                    style = KptTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (state.fullName != null || state.name == NameState.Loading) {
                Text(
                    state.username,
                    style = KptTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.testTag(TestTags.Profile.USERNAME),
                )
            } else {
                Text(state.officeName, style = KptTheme.typography.bodyMedium, color = c.onSurfaceVariant)
            }
            state.email?.let {
                Text(
                    it,
                    style = KptTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (state.name == NameState.Loading) {
                KptShimmerLoadingBox(Modifier.fillMaxWidth(0.55f).heightIn(min = KptTheme.spacing.md))
            }
            if (state.hasStaffLink) StaffChip(state.isLoanOfficer == true)
        }
    }
}

@Composable
private fun StaffChip(loanOfficer: Boolean) {
    val c = KptTheme.colorScheme
    Text(
        stringResource(if (loanOfficer) Res.string.feature_profile_loan_officer else Res.string.feature_profile_staff),
        style = KptTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = c.onSecondaryContainer,
        modifier = Modifier
            .padding(top = KptTheme.spacing.xs)
            .background(c.secondaryContainer, CircleShape)
            .padding(
                horizontal = KptTheme.spacing.sm + KptTheme.spacing.xs / 2,
                vertical = KptTheme.spacing.xs,
            )
            .testTag(TestTags.Profile.STAFF_CHIP),
    )
}

@Composable
private fun NameError(reason: String?, onRetry: () -> Unit) {
    val c = KptTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.errorContainer, KptTheme.shapes.medium)
            .padding(start = KptTheme.spacing.md, end = KptTheme.spacing.xs)
            .testTag(TestTags.Profile.NAME_ERROR),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm + KptTheme.spacing.xs),
    ) {
        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = c.onErrorContainer)
        Column(Modifier.weight(1f).padding(vertical = KptTheme.spacing.sm)) {
            Text(
                stringResource(Res.string.feature_profile_name_error),
                style = KptTheme.typography.bodyMedium,
                color = c.onErrorContainer,
            )
            Text(
                reason ?: stringResource(Res.string.feature_profile_name_error_generic),
                style = KptTheme.typography.bodySmall,
                color = c.onErrorContainer,
            )
        }
        TextButton(onClick = onRetry, modifier = Modifier.testTag(TestTags.Profile.RETRY)) {
            Text(stringResource(Res.string.feature_profile_retry), fontWeight = FontWeight.SemiBold, color = c.error)
        }
    }
}

@Composable
private fun OfficeAndScope(state: ProfileUiState) {
    Group {
        Item(
            Icons.Outlined.Apartment,
            label = stringResource(Res.string.feature_profile_office_label),
            value = state.officeName,
        )
        Divider()
        Item(
            Icons.Outlined.Insights,
            label = stringResource(Res.string.feature_profile_scope_title),
            value = if (state.hasStaffLink) {
                stringResource(Res.string.feature_profile_scope_my_portfolio)
            } else {
                stringResource(Res.string.feature_profile_scope_office, state.officeName)
            },
            supporting = stringResource(Res.string.feature_profile_scope_note),
            loading = state.name == NameState.Loading,
            modifier = Modifier.testTag(TestTags.Profile.SCOPE),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Roles(state: ProfileUiState) {
    val c = KptTheme.colorScheme
    Section(stringResource(Res.string.feature_profile_roles_title), Modifier.testTag(TestTags.Profile.ROLES)) {
        FlowRow(
            modifier = Modifier.padding(KptTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            when {
                state.roles.isNotEmpty() -> state.roles.forEach { role ->
                    Row(
                        modifier = Modifier
                            .border(KptTheme.spacing.xs / 4, c.outlineVariant, KptTheme.shapes.small)
                            .padding(
                                horizontal = KptTheme.spacing.sm + KptTheme.spacing.xs,
                                vertical = KptTheme.spacing.xs + KptTheme.spacing.xs / 2,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs + KptTheme.spacing.xs / 2),
                    ) {
                        Icon(
                            Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = c.primary,
                            modifier = Modifier.size(KptTheme.spacing.md + KptTheme.spacing.xs / 2),
                        )
                        Text(role, style = KptTheme.typography.labelLarge)
                    }
                }
                state.name == NameState.Loading -> KptShimmerLoadingBox(
                    Modifier.fillMaxWidth(0.4f).heightIn(min = KptTheme.spacing.lg + KptTheme.spacing.sm),
                )
                else -> Text(
                    stringResource(Res.string.feature_profile_roles_empty),
                    style = KptTheme.typography.bodyMedium,
                    color = c.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Session(state: ProfileUiState, onServerSettings: () -> Unit) {
    val c = KptTheme.colorScheme
    Section(stringResource(Res.string.feature_profile_session_title), Modifier.testTag(TestTags.Profile.SERVER)) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = ROW_MIN).padding(horizontal = KptTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            Icon(Icons.Outlined.Dns, contentDescription = null, tint = c.onSurfaceVariant)
            Text(
                state.server.instanceName,
                style = KptTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false),
            )
            // "Live" = this is the published instance the session is on; offline it cannot be live.
            if (state.server.known && state.online) {
                Text(
                    stringResource(Res.string.feature_profile_live),
                    style = KptTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = c.onSecondaryContainer,
                    modifier = Modifier
                        .background(c.secondaryContainer, CircleShape)
                        .padding(horizontal = KptTheme.spacing.sm, vertical = KptTheme.spacing.xs / 2)
                        .testTag(TestTags.Profile.LIVE),
                )
            }
        }
        Divider()
        Item(
            Icons.Outlined.Link,
            label = stringResource(Res.string.feature_profile_server_label),
            value = state.server.url,
        )
        Divider()
        Item(Icons.Outlined.Tag, label = stringResource(Res.string.feature_profile_tenant), value = state.server.tenant)
        Divider()
        ActionItem(
            Icons.Outlined.Tune,
            label = stringResource(Res.string.feature_profile_server_settings),
            color = c.primary,
            onClick = onServerSettings,
            modifier = Modifier.testTag(TestTags.Profile.SERVER_SETTINGS),
        )
    }
}

@Composable
private fun Account(state: ProfileUiState, onChangePassword: () -> Unit, onSignOut: () -> Unit) {
    val c = KptTheme.colorScheme
    Group(Modifier.testTag(TestTags.Profile.ACTIONS)) {
        ActionItem(
            Icons.Outlined.LockReset,
            label = stringResource(Res.string.feature_profile_change_password),
            supporting = if (state.online) null else stringResource(Res.string.feature_profile_change_password_offline),
            enabled = state.online,
            onClick = onChangePassword,
            modifier = Modifier.testTag(TestTags.Profile.CHANGE_PASSWORD),
        )
        Divider()
        ActionItem(
            Icons.AutoMirrored.Outlined.Logout,
            label = stringResource(Res.string.feature_profile_sign_out),
            color = c.error,
            chevron = false,
            onClick = onSignOut,
            modifier = Modifier.testTag(TestTags.Profile.SIGN_OUT),
        )
    }
}

/**
 * Fineract names are often upper-case ("IRVING ZEPEDA"); read them as a name. Same rule as Home's
 * greeting.
 */
private fun String.toNameCase(): String =
    if (this == uppercase()) {
        lowercase().split(' ').joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
    } else {
        this
    }
