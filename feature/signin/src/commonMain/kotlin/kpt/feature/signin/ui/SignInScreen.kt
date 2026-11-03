/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppPasswordToggle
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.utils.DemoCredentials
import kpt.core.ui.components.AppProgressIndicatorOverlay
import kpt.feature.signin.BuildKonfig
import kpt.feature.signin.generated.resources.Res
import kpt.feature.signin.generated.resources.feature_signin_attempts_remaining
import kpt.feature.signin.generated.resources.feature_signin_authenticating
import kpt.feature.signin.generated.resources.feature_signin_biometric_unlock
import kpt.feature.signin.generated.resources.feature_signin_brand_mark
import kpt.feature.signin.generated.resources.feature_signin_cd_change_instance
import kpt.feature.signin.generated.resources.feature_signin_cd_logo
import kpt.feature.signin.generated.resources.feature_signin_cd_password_error
import kpt.feature.signin.generated.resources.feature_signin_cd_username_error
import kpt.feature.signin.generated.resources.feature_signin_consent
import kpt.feature.signin.generated.resources.feature_signin_demo_credentials
import kpt.feature.signin.generated.resources.feature_signin_enter_credentials
import kpt.feature.signin.generated.resources.feature_signin_error_biometric_failed
import kpt.feature.signin.generated.resources.feature_signin_error_login_failed
import kpt.feature.signin.generated.resources.feature_signin_error_offline
import kpt.feature.signin.generated.resources.feature_signin_error_password_length
import kpt.feature.signin.generated.resources.feature_signin_error_unreachable
import kpt.feature.signin.generated.resources.feature_signin_error_username_length
import kpt.feature.signin.generated.resources.feature_signin_locked_out
import kpt.feature.signin.generated.resources.feature_signin_login
import kpt.feature.signin.generated.resources.feature_signin_password
import kpt.feature.signin.generated.resources.feature_signin_password_placeholder
import kpt.feature.signin.generated.resources.feature_signin_set_instance
import kpt.feature.signin.generated.resources.feature_signin_username
import kpt.feature.signin.generated.resources.feature_signin_username_placeholder
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sign-in screen.
 *
 * **UI source: the stitch renders under `idea-layer/mockups/signin/stitch/`** — the highest design
 * tier on disk, so per RULE-UI-SOURCE-001 US9 this file is converted from them, not from `ui.yaml`.
 * Resolution is recorded per state in `idea-layer/state/ui-source-ledger.jsonl` (all six: `stitch`).
 *
 * Structurally this restores four things v1 had that the earlier port had lost, each of which the
 * 2026-10-02 re-extraction found MISSING FROM THE DOCSET TOO, which is why no gate caught them:
 *
 *  - **D1** the server-config CTA is a pinned BOTTOM BAR. The previous version put it at the top of
 *    the form and its own comment claimed that was "as in the original" — v1 uses
 *    `Scaffold(bottomBar = …)`.
 *  - **D4** submit renders a MODAL overlay over the whole form, not an inline spinner replacing the
 *    button. Both prevent a double submit; only the overlay also blocks the fields.
 *  - **D2/D3** each field shows a trailing error icon when its own rule failed, and on the password
 *    field that icon REPLACES the visibility toggle rather than sitting beside it.
 *  - **D12** no top app bar and no chrome above the logo — this is the unauthenticated gate, so a
 *    settings or back affordance here would lead nowhere.
 *
 * Two deliberate deviations from the renders, both judgement calls rather than omissions:
 *
 *  1. **Field chrome is the design system's, not the mockup's.** The renders draw a label ABOVE each
 *     input; `AppOutlinedTextField` uses a Material floating label. Matching the render pixel-wise
 *     would mean hand-rolling a text field inside a feature module — duplicating `core/designsystem`
 *     and bypassing the design tokens, which is the duplication RULE-IMPL-DUPLICATION-001 and
 *     RULE-IMPL-DESIGN-TOKEN-CONSUMPTION-001 exist to stop. The affordances the render specifies
 *     (leading icon, trailing toggle/error, placeholder, error text) are all honored.
 *  2. **The logo is the real asset.** The renders substitute an `account_balance` glyph because the
 *     generator cannot embed our JPEG; `ui.yaml#components.logo` binds
 *     `feature_signin_brand_mark`. The render's CONTAINER treatment (a soft rounded tinted tile) is
 *     adopted; the thing inside it is the declared asset.
 */
@Composable
fun SignInScreen(
    onLoggedIn: () -> Unit,
    onUpdateServerConfig: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                SignInEvent.LoggedIn -> onLoggedIn()
            }
        }
    }

    SignInContent(
        state = state,
        onAction = { viewModel.actionChannel.trySend(it) },
        onUpdateServerConfig = onUpdateServerConfig,
        modifier = modifier,
    )
}

@Composable
internal fun SignInContent(
    state: SignInState,
    onAction: (SignInAction) -> Unit,
    onUpdateServerConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Only transport-level failures reach the Snackbar; field problems render on their field.
    val transportError = state.error?.takeIf { it.isTransportError }
    val transportMessage = transportError?.let { stringResource(it.message) }
    LaunchedEffect(transportError, transportMessage) {
        if (transportMessage != null) snackbarHostState.showSnackbar(transportMessage)
    }

    // ui.yaml#components.password_field.trailing_slot declares this `local_state` on purpose:
    // whether the characters are on screen is a property of this rendering, not of the sign-in
    // attempt, and it must not survive into the session.
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // D12: no topBar. The unauthenticated gate has nowhere to navigate back to.
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // ui.yaml#components.consent_text — ABSENT in v1 and the clearest professional
            // omission: an app that collects credentials and transmits banking data states its
            // terms at the point of collection. Pinned, never scrollable: a consent the user
            // cannot read is not a consent.
            Text(
                text = stringResource(Res.string.feature_signin_consent),
                style = MaterialTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag(SignInTestTags.CONSENT),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .testTag(SignInTestTags.SCREEN),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // ui.yaml#components.instance_chip — REPLACES v1's blind "Update Server
                // Configuration" button. That button offered to change a setting without ever
                // showing its current value, so a misconfigured instance was indistinguishable
                // from a correct one. Information first, affordance second.
                //
                // Rendered UNCONDITIONALLY. An earlier version hid it while `instanceHost` was
                // blank, which inverted the intent: a blank host is the FIRST-RUN case, the one
                // where this is the only control that can get the user unstuck. Hiding the single
                // route to server configuration precisely when no server is configured strands
                // them — the same class of defect as v1's blind button, wearing a different shape.
                InstanceChip(
                    host = state.instanceHost,
                    tenant = state.instanceTenant,
                    onClick = onUpdateServerConfig,
                )
                // A public demo instance's published sign-ins, one chip per role — tap to fill. Absent on
                // a real server.
                if (state.demoAccounts.isNotEmpty()) {
                    DemoAccounts(accounts = state.demoAccounts, onPick = { onAction(SignInAction.UseDemoAccount(it)) })
                }

                LogoTile()

                TitleBlock()

                UsernameField(state = state, onAction = onAction)

                PasswordField(
                    state = state,
                    onAction = onAction,
                    passwordVisible = passwordVisible,
                    onVisibleChange = { passwordVisible = it },
                )

                // ui.yaml#components.lockout_notice. Two forms: a warning as the limit nears, and
                // the lock itself. Both state what happens next, because a throttle with no stated
                // exit reads as a dead app.
                if (state.isLockedOut || state.isApproachingLockout) {
                    LockoutNotice(
                        locked = state.isLockedOut,
                        attemptsRemaining = state.attemptsRemaining,
                    )
                }

                // The button STAYS, enabled-gated, and keeps its label while submitting — the
                // overlay below is what blocks a second tap. The previous port swapped the button
                // for a spinner, which left the bottom-bar CTA as the only thing on screen that
                // still looked pressable during a credential check.
                Button(
                    onClick = { onAction(SignInAction.Submit) },
                    // Disabled, never hidden: removing the control shifts the layout under the
                    // user's thumb at the exact moment they are being told to stop.
                    enabled = !state.isSubmitting && !state.isLockedOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag(SignInTestTags.SUBMIT),
                ) {
                    Text(
                        stringResource(
                            if (state.isSubmitting) {
                                Res.string.feature_signin_authenticating
                            } else {
                                Res.string.feature_signin_login
                            },
                        ),
                    )
                    if (!state.isSubmitting) {
                        Spacer(Modifier.size(8.dp))
                        Icon(imageVector = AppIcons.ArrowForward, contentDescription = null)
                    }
                }

                // `ui.yaml#components.biometric_action` — secondary, and rendered only in the
                // `biometric_ready` state. Hidden rather than disabled: unlike the submit button,
                // this control has no meaning for a user with no stored session, and a permanently
                // greyed affordance reads as a broken feature.
                if (state.canUseBiometrics) {
                    TextButton(
                        onClick = { onAction(SignInAction.UnlockWithBiometrics) },
                        enabled = !state.isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(SignInTestTags.BIOMETRIC),
                    ) {
                        Icon(
                            imageVector = AppIcons.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(Res.string.feature_signin_biometric_unlock))
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // D4 — v1's `AppProgressIndicatorOverlay()`, the last child of the Scaffold body.
            // Modal and non-dismissible: every terminal state (success, error, offline) clears
            // `isSubmitting`, so it is never dismissed by the user.
            if (state.isSubmitting) {
                AppProgressIndicatorOverlay(
                    modifier = Modifier.fillMaxSize().testTag(SignInTestTags.PROGRESS_OVERLAY),
                )
            }
        }
    }
}

@Composable
private fun UsernameField(state: SignInState, onAction: (SignInAction) -> Unit) {
    AppOutlinedTextField(
        value = state.username,
        onValueChange = { onAction(SignInAction.UsernameChanged(it)) },
        label = stringResource(Res.string.feature_signin_username),
        placeholder = stringResource(Res.string.feature_signin_username_placeholder),
        leadingIcon = AppIcons.Person,
        enabled = !state.isSubmitting,
        // Fineract usernames are case-sensitive and the shared field defaults to
        // capitalising words, so an IME turned `fieldofficer` into `Fieldofficer` and
        // the server rejected it with a message the user could do nothing about.
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
        isError = state.error == SignInError.UsernameTooShort,
        errorText = (state.error == SignInError.UsernameTooShort)
            .takeIf { it }
            ?.let { stringResource(Res.string.feature_signin_error_username_length) },
        // D2 — v1 renders a trailing error icon on this field when its rule failed.
        trailingIcon = if (state.error == SignInError.UsernameTooShort) {
            {
                Icon(
                    imageVector = AppIcons.Error,
                    contentDescription = stringResource(Res.string.feature_signin_cd_username_error),
                    tint = KptTheme.colorScheme.error,
                    modifier = Modifier.testTag(SignInTestTags.USERNAME_ERROR_ICON),
                )
            }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth().testTag(SignInTestTags.USERNAME),
    )
}

@Composable
private fun PasswordField(
    state: SignInState,
    onAction: (SignInAction) -> Unit,
    passwordVisible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
) {
    val passwordInvalid = state.error == SignInError.PasswordTooShort
    AppOutlinedTextField(
        value = state.password,
        onValueChange = { onAction(SignInAction.PasswordChanged(it)) },
        label = stringResource(Res.string.feature_signin_password),
        placeholder = stringResource(Res.string.feature_signin_password_placeholder),
        leadingIcon = AppIcons.Lock,
        enabled = !state.isSubmitting,
        keyboardType = KeyboardType.Password,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
        // D3 — the trailing slot is MUTUALLY EXCLUSIVE in v1: the visibility toggle only
        // while there is no error, an error icon instead when there is one. Disabling
        // the toggle is what hands the slot over.
        passwordToggle = AppPasswordToggle(
            isVisible = passwordVisible,
            onClick = { onVisibleChange(it) },
            isDisplayed = !passwordInvalid,
        ),
        isError = passwordInvalid,
        errorText = passwordInvalid
            .takeIf { it }
            ?.let { stringResource(Res.string.feature_signin_error_password_length) },
        trailingIcon = if (passwordInvalid) {
            {
                Icon(
                    imageVector = AppIcons.Error,
                    contentDescription = stringResource(Res.string.feature_signin_cd_password_error),
                    tint = KptTheme.colorScheme.error,
                    modifier = Modifier.testTag(SignInTestTags.PASSWORD_ERROR_ICON),
                )
            }
        } else {
            null
        },
        modifier = Modifier.fillMaxWidth().testTag(SignInTestTags.PASSWORD),
    )
}

/**
 * The render's logo treatment: a 64dp soft rounded tinted tile. The thing inside it is the asset
 * `ui.yaml#components.logo` declares, not the render's substitute glyph (see the file header).
 */
/**
 * The instance this sign-in will hit: host and tenant, tappable to change.
 *
 * Deliberately an `AssistChip` and not a Button. `components.yaml#assist_chip` states the rule:
 * information first, affordance second. It must read as a status line, not compete with the primary
 * action. The v1 control it replaces was a full-width filled button that said only "Update Server
 * Configuration" and never showed what the configuration WAS.
 */
@Composable
private fun LockoutNotice(locked: Boolean, attemptsRemaining: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = KptTheme.colorScheme.tertiaryContainer,
        contentColor = KptTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier.fillMaxWidth().testTag(SignInTestTags.LOCKOUT_NOTICE),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(imageVector = AppIcons.Error, contentDescription = null)
            Text(
                text = if (locked) {
                    stringResource(Res.string.feature_signin_locked_out)
                } else {
                    stringResource(Res.string.feature_signin_attempts_remaining, attemptsRemaining)
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DemoAccounts(accounts: List<DemoCredentials>, onPick: (DemoCredentials) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().testTag(SignInTestTags.DEMO_CREDENTIALS),
    ) {
        Text(
            text = stringResource(Res.string.feature_signin_demo_credentials),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            accounts.forEach { account ->
                SuggestionChip(
                    onClick = { onPick(account) },
                    label = { Text(account.role) },
                )
            }
        }
    }
}

@Composable
private fun InstanceChip(
    host: String,
    tenant: String,
    onClick: () -> Unit,
) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(
                // Blank host = the preference flow has not emitted yet, or genuinely nothing is
                // configured. Either way the chip states what it DOES rather than rendering a
                // bare separator, so it never reads as a glitch.
                text = if (host.isBlank()) {
                    stringResource(Res.string.feature_signin_set_instance)
                } else {
                    "$host · $tenant"
                },
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = AppIcons.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = AppIcons.ArrowForward,
                contentDescription = stringResource(Res.string.feature_signin_cd_change_instance),
                modifier = Modifier.size(18.dp),
            )
        },
        modifier = Modifier.heightIn(min = 36.dp).testTag(SignInTestTags.INSTANCE_CHIP),
    )
}

@Composable
private fun LogoTile() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = KptTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(64.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Compose's own `Image`, NOT the design system's `AppAndroidClientIcon`, for two
            // reasons that are both about that wrapper rather than about preference: it hardcodes
            // `contentDescription = null` — so the logo can never be announced, and the a11y rule
            // cannot be satisfied through it — and it forces a fixed 200x100dp, which cannot sit
            // inside the render's 64dp tile. Fixing the wrapper means editing `core/designsystem`,
            // a template-derived module, so it belongs in an upstream PR rather than here.
            Image(
                painter = painterResource(Res.drawable.feature_signin_brand_mark),
                contentDescription = stringResource(Res.string.feature_signin_cd_logo),
                modifier = Modifier.size(40.dp).testTag(SignInTestTags.LOGO),
            )
        }
    }
}

/**
 * Two lines, per the render: the app name as headline, then v1's prompt.
 *
 * The headline comes from `BuildKonfig.APP_DISPLAY_NAME` (→ `app-profile/app.yaml#identity.app_name`)
 * rather than a string resource, so a fork rebranding in app-profile does not have to find this
 * screen. The prompt keeps v1's own resource; the render's version of that line read "Sign in to
 * access your field officer account", which is sample copy, not the shipped string.
 */
@Composable
private fun TitleBlock() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().testTag(SignInTestTags.TITLE),
    ) {
        Text(
            text = BuildKonfig.APP_DISPLAY_NAME,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.feature_signin_enter_credentials),
            style = MaterialTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * True for failures that are about reaching the server, not about what the user typed.
 *
 * Drives the Snackbar-vs-field split: a transport failure has no field to attach to, while
 * "invalid username length" does.
 */
private val SignInError.isTransportError: Boolean
    get() = when (this) {
        SignInError.Rejected, SignInError.Offline, SignInError.Unreachable -> true
        // LockedOut renders as an in-flow banner, not a transient snackbar: a message that
        // disappears cannot tell you when the lock clears.
        SignInError.LockedOut -> false
        // Transport-shaped: a snackbar is right here. It is NOT an input error (nothing the user
        // typed is wrong) and NOT in-flow like lockout (the form stays fully usable).
        SignInError.BiometricFailed -> true
        SignInError.UsernameTooShort, SignInError.PasswordTooShort -> false
    }

private val SignInError.message: StringResource
    get() = when (this) {
        SignInError.UsernameTooShort -> Res.string.feature_signin_error_username_length
        SignInError.PasswordTooShort -> Res.string.feature_signin_error_password_length
        SignInError.Rejected -> Res.string.feature_signin_error_login_failed
        SignInError.Offline -> Res.string.feature_signin_error_offline
        SignInError.Unreachable -> Res.string.feature_signin_error_unreachable
        SignInError.LockedOut -> Res.string.feature_signin_locked_out
        SignInError.BiometricFailed -> Res.string.feature_signin_error_biometric_failed
    }
