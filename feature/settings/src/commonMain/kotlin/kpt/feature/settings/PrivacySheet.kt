/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.platform.LocalUrlLauncher
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.spacing
import kpt.core.model.config.AppPrivacyConfig
import kpt.feature.settings.generated.resources.Res
import kpt.feature.settings.generated.resources.feature_settings_loading
import kpt.feature.settings.generated.resources.feature_settings_privacy
import kpt.feature.settings.generated.resources.feature_settings_privacy_analytics
import kpt.feature.settings.generated.resources.feature_settings_privacy_analytics_description
import kpt.feature.settings.generated.resources.feature_settings_privacy_crash_reports
import kpt.feature.settings.generated.resources.feature_settings_privacy_crash_reports_description
import kpt.feature.settings.generated.resources.feature_settings_privacy_intro
import kpt.feature.settings.generated.resources.feature_settings_privacy_policy
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings → Privacy, as a bottom sheet: the user's consent to usage analytics and crash reporting, each one
 * switch, plus the link to the hosted privacy policy.
 *
 * Ported from the template (`openMF/kmp-project-template` `feature/settings/PrivacySheet.kt`): this fork's
 * `feature/settings` is fork-owned and never synced, so the template's privacy sheet never arrived on its own.
 * It is also where onboarding's "you can change this later in Settings" note points, and the only way an
 * install that predates onboarding can opt in at all.
 *
 * Its own entry on the Settings screen — not a section inside another dialog — because withdrawing consent
 * must be as easy as giving it (GDPR Art. 7(3)): one tap from Settings, labelled for what it is. Both
 * switches start off unless `app-profile/app.yaml#privacy` says otherwise, and a change applies at once
 * (`PrivacyConsentApplier` drives the Firebase library).
 *
 * The privacy POLICY itself is not in the app: it lives on the hosted page (`app-profile/app.yaml#org.privacy_url`,
 * the same page the store listings link) and opens in the in-app browser — Custom Tabs on Android,
 * SFSafariViewController on iOS — so the text is updated without a release.
 */
@Composable
fun PrivacySheet(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val urlLauncher = LocalUrlLauncher.current
    PrivacySheet(
        settingsState = settingsState,
        onDismiss = onDismiss,
        onRetry = viewModel::onRetry,
        onChangeAnalyticsConsent = viewModel::updateAnalyticsConsent,
        onChangeCrashReportingConsent = viewModel::updateCrashReportingConsent,
        // No link when the fork has not set org.privacy_url, rather than one that opens nothing.
        onOpenPrivacyPolicy = AppPrivacyConfig.PRIVACY_POLICY_URL.takeIf { it.isNotEmpty() }?.let { url ->
            { urlLauncher.launchInApp(url) }
        },
    )
}

/** Privacy sheet over explicit state — the testable overload. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySheet(
    settingsState: ScreenState<UserEditableSettings>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeAnalyticsConsent: (granted: Boolean) -> Unit,
    onChangeCrashReportingConsent: (granted: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onOpenPrivacyPolicy: (() -> Unit)? = null,
) {
    val sp = MaterialTheme.spacing
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(TestTags.Privacy.SHEET),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = sp.lg, end = sp.lg, bottom = sp.lg),
            verticalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(sp.md)) {
                Icon(
                    imageVector = AppIcons.OutlinedLock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(Res.string.feature_settings_privacy),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                text = stringResource(Res.string.feature_settings_privacy_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            ScreenContent(
                state = settingsState,
                onRetry = onRetry,
                loading = { Text(text = stringResource(Res.string.feature_settings_loading)) },
            ) { settings, _ ->
                Column(verticalArrangement = Arrangement.spacedBy(sp.sm)) {
                    ConsentSwitchRow(
                        title = stringResource(Res.string.feature_settings_privacy_analytics),
                        description = stringResource(Res.string.feature_settings_privacy_analytics_description),
                        checked = settings.analyticsConsent,
                        onCheckedChange = onChangeAnalyticsConsent,
                        modifier = Modifier.testTag(TestTags.Privacy.ANALYTICS_SWITCH),
                    )
                    ConsentSwitchRow(
                        title = stringResource(Res.string.feature_settings_privacy_crash_reports),
                        description = stringResource(Res.string.feature_settings_privacy_crash_reports_description),
                        checked = settings.crashReportingConsent,
                        onCheckedChange = onChangeCrashReportingConsent,
                        modifier = Modifier.testTag(TestTags.Privacy.CRASH_REPORTS_SWITCH),
                    )
                }
            }
            onOpenPrivacyPolicy?.let { open ->
                HorizontalDivider()
                TextButton(onClick = open, modifier = Modifier.testTag(TestTags.Privacy.POLICY_LINK)) {
                    Icon(imageVector = AppIcons.OutlinedInfo, contentDescription = null)
                    Text(
                        text = stringResource(Res.string.feature_settings_privacy_policy),
                        modifier = Modifier.padding(start = sp.sm),
                    )
                }
            }
        }
    }
}

/**
 * One consent: what is shared (title) and why (description), with a switch. The WHOLE row toggles, with the
 * switch role, so it is one large touch target and reads as a single control to a screen reader.
 */
@Composable
internal fun ConsentSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = sp.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(sp.md),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sp.xs)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        // onCheckedChange = null: the row owns the toggle, so the switch never double-fires.
        Switch(checked = checked, onCheckedChange = null)
    }
}
