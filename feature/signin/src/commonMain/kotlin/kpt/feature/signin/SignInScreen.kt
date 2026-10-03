/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import androidx.compose.foundation.Image
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.ui.components.MifosProgressIndicatorOverlay
import kpt.feature.signin.generated.resources.Res
import kpt.feature.signin.generated.resources.feature_signin_authenticating
import kpt.feature.signin.generated.resources.feature_signin_cd_logo
import kpt.feature.signin.generated.resources.feature_signin_cd_password_error
import kpt.feature.signin.generated.resources.feature_signin_cd_username_error
import kpt.feature.signin.generated.resources.feature_signin_enter_credentials
import kpt.feature.signin.generated.resources.feature_signin_error_login_failed
import kpt.feature.signin.generated.resources.feature_signin_error_offline
import kpt.feature.signin.generated.resources.feature_signin_error_password_length
import kpt.feature.signin.generated.resources.feature_signin_error_username_length
import kpt.feature.signin.generated.resources.feature_signin_login
import kpt.feature.signin.generated.resources.feature_signin_brand_mark
import kpt.feature.signin.generated.resources.feature_signin_password
import kpt.feature.signin.generated.resources.feature_signin_password_placeholder
import kpt.feature.signin.generated.resources.feature_signin_update_server_config
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
 *     input; `MifosOutlinedTextField` uses a Material floating label. Matching the render pixel-wise
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
            // D1 — v1's `Scaffold(bottomBar = …)`: pinned, centered, tonal. Deliberately NOT
            // disabled while submitting: it sits outside the overlay's parent in v1 too, and a user
            // whose server is misconfigured needs the way out precisely when sign-in is failing.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                FilledTonalButton(
                    onClick = onUpdateServerConfig,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = KptTheme.colorScheme.secondaryContainer,
                        contentColor = KptTheme.colorScheme.onSecondaryContainer,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag(SignInTestTags.SERVER_CONFIG),
                ) {
                    Icon(imageVector = MifosIcons.Settings, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(Res.string.feature_signin_update_server_config))
                }
            }
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
                LogoTile()

                TitleBlock()

                MifosOutlinedTextField(
                    value = state.username,
                    onValueChange = { onAction(SignInAction.UsernameChanged(it)) },
                    label = stringResource(Res.string.feature_signin_username),
                    placeholder = stringResource(Res.string.feature_signin_username_placeholder),
                    leadingIcon = MifosIcons.Person,
                    enabled = !state.isSubmitting,
                    // Fineract usernames are case-sensitive and the shared field defaults to
                    // capitalising words, so an IME turned `fieldofficer` into `Fieldofficer` and
                    // the server rejected it with a message the user could do nothing about.
                    capitalization = KeyboardCapitalization.None,
                    isError = state.error == SignInError.UsernameTooShort,
                    errorText = (state.error == SignInError.UsernameTooShort)
                        .takeIf { it }
                        ?.let { stringResource(Res.string.feature_signin_error_username_length) },
                    // D2 — v1 renders a trailing error icon on this field when its rule failed.
                    trailingIcon = if (state.error == SignInError.UsernameTooShort) {
                        {
                            Icon(
                                imageVector = MifosIcons.Error,
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

                val passwordInvalid = state.error == SignInError.PasswordTooShort
                MifosOutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(SignInAction.PasswordChanged(it)) },
                    label = stringResource(Res.string.feature_signin_password),
                    placeholder = stringResource(Res.string.feature_signin_password_placeholder),
                    leadingIcon = MifosIcons.Lock,
                    enabled = !state.isSubmitting,
                    keyboardType = KeyboardType.Password,
                    capitalization = KeyboardCapitalization.None,
                    // D3 — the trailing slot is MUTUALLY EXCLUSIVE in v1: the visibility toggle only
                    // while there is no error, an error icon instead when there is one. Disabling
                    // the toggle is what hands the slot over.
                    isPasswordToggleDisplayed = !passwordInvalid,
                    isPasswordVisible = passwordVisible,
                    onPasswordToggleClick = { passwordVisible = it },
                    isError = passwordInvalid,
                    errorText = passwordInvalid
                        .takeIf { it }
                        ?.let { stringResource(Res.string.feature_signin_error_password_length) },
                    trailingIcon = if (passwordInvalid) {
                        {
                            Icon(
                                imageVector = MifosIcons.Error,
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

                // The button STAYS, enabled-gated, and keeps its label while submitting — the
                // overlay below is what blocks a second tap. The previous port swapped the button
                // for a spinner, which left the bottom-bar CTA as the only thing on screen that
                // still looked pressable during a credential check.
                Button(
                    onClick = { onAction(SignInAction.Submit) },
                    enabled = !state.isSubmitting,
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
                        Icon(imageVector = MifosIcons.ArrowForward, contentDescription = null)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // D4 — v1's `MifosProgressIndicatorOverlay()`, the last child of the Scaffold body.
            // Modal and non-dismissible: every terminal state (success, error, offline) clears
            // `isSubmitting`, so it is never dismissed by the user.
            if (state.isSubmitting) {
                MifosProgressIndicatorOverlay(
                    modifier = Modifier.fillMaxSize().testTag(SignInTestTags.PROGRESS_OVERLAY),
                )
            }
        }
    }
}

/**
 * The render's logo treatment: a 64dp soft rounded tinted tile. The thing inside it is the asset
 * `ui.yaml#components.logo` declares, not the render's substitute glyph (see the file header).
 */
@Composable
private fun LogoTile() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = KptTheme.colorScheme.secondaryContainer,
        modifier = Modifier.size(64.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Compose's own `Image`, NOT the design system's `MifosAndroidClientIcon`, for two
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
        SignInError.Rejected, SignInError.Offline -> true
        SignInError.UsernameTooShort, SignInError.PasswordTooShort -> false
    }

private val SignInError.message: StringResource
    get() = when (this) {
        SignInError.UsernameTooShort -> Res.string.feature_signin_error_username_length
        SignInError.PasswordTooShort -> Res.string.feature_signin_error_password_length
        SignInError.Rejected -> Res.string.feature_signin_error_login_failed
        SignInError.Offline -> Res.string.feature_signin_error_offline
    }
