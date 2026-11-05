/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.motion
import kpt.core.base.platform.LocalUrlLauncher
import kpt.core.base.ui.motion.KptSharedAxis
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.spacing
import kpt.core.model.config.AppOrgLinks
import kpt.core.model.config.AppPrivacyConfig
import kpt.feature.onboarding.generated.resources.Res
import kpt.feature.onboarding.generated.resources.feature_onboarding_agreement
import kpt.feature.onboarding.generated.resources.feature_onboarding_allow_get_started
import kpt.feature.onboarding.generated.resources.feature_onboarding_analytics_body
import kpt.feature.onboarding.generated.resources.feature_onboarding_analytics_label
import kpt.feature.onboarding.generated.resources.feature_onboarding_brand_a11y
import kpt.feature.onboarding.generated.resources.feature_onboarding_clients_body
import kpt.feature.onboarding.generated.resources.feature_onboarding_clients_title
import kpt.feature.onboarding.generated.resources.feature_onboarding_collections_body
import kpt.feature.onboarding.generated.resources.feature_onboarding_collections_title
import kpt.feature.onboarding.generated.resources.feature_onboarding_connect_instance
import kpt.feature.onboarding.generated.resources.feature_onboarding_continue_choices
import kpt.feature.onboarding.generated.resources.feature_onboarding_continue_without
import kpt.feature.onboarding.generated.resources.feature_onboarding_crash_body
import kpt.feature.onboarding.generated.resources.feature_onboarding_crash_label
import kpt.feature.onboarding.generated.resources.feature_onboarding_next
import kpt.feature.onboarding.generated.resources.feature_onboarding_no_client_data
import kpt.feature.onboarding.generated.resources.feature_onboarding_offline_body
import kpt.feature.onboarding.generated.resources.feature_onboarding_offline_title
import kpt.feature.onboarding.generated.resources.feature_onboarding_page_of
import kpt.feature.onboarding.generated.resources.feature_onboarding_privacy_lead
import kpt.feature.onboarding.generated.resources.feature_onboarding_privacy_policy
import kpt.feature.onboarding.generated.resources.feature_onboarding_privacy_title
import kpt.feature.onboarding.generated.resources.feature_onboarding_save_failed
import kpt.feature.onboarding.generated.resources.feature_onboarding_settings_later
import kpt.feature.onboarding.generated.resources.feature_onboarding_skip
import kpt.feature.onboarding.generated.resources.feature_onboarding_skip_a11y
import kpt.feature.onboarding.generated.resources.feature_onboarding_terms
import kpt.feature.onboarding.generated.resources.onboarding_brand_mark
import kpt.feature.onboarding.generated.resources.onboarding_clients_illustration
import kpt.feature.onboarding.generated.resources.onboarding_collections_illustration
import kpt.feature.onboarding.generated.resources.onboarding_offline_illustration
import kpt.feature.onboarding.generated.resources.onboarding_privacy_illustration
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Responsive breakpoints from ui.yaml#layout.responsive (compact < 600, medium 600–839, expanded ≥ 840) and the
 * declared content widths. Layout constraints, not spacing — the spacing scale has no 560/1040 step.
 */
private val MediumBreakpoint: Dp = 600.dp // design-token:skip — window-size breakpoint
private val ExpandedBreakpoint: Dp = 840.dp // design-token:skip — window-size breakpoint
private val MediumContentWidth: Dp = 560.dp // design-token:skip — ui.yaml medium max content width
private val ExpandedContentWidth: Dp = 1040.dp // design-token:skip — ui.yaml expanded max content width

/**
 * First-run onboarding (idea-layer/screens/onboarding): three intro pages and the privacy page. Shown once, for
 * `RootNavState.ShowOnboarding`; finishing it marks first run done, and RootNav moves to sign-in on its own.
 *
 * Design source: the Stitch renders under idea-layer/mockups/onboarding/stitch/ (ui-source-ledger rows).
 * The privacy-policy and terms links open IN-APP (Custom Tabs / SFSafariViewController) from generated config —
 * `AppPrivacyConfig.PRIVACY_POLICY_URL` and `AppOrgLinks["terms"]` — never a literal.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun OnboardingScreen(
    onOpenServerConfig: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val urlLauncher = LocalUrlLauncher.current

    // System back steps to the previous page; on the first page it falls through to the system, which leaves
    // without saving — both choices stay off and onboarding shows again next launch.
    BackHandler(enabled = state.page != OnboardingPage.Clients) {
        viewModel.trySendAction(OnboardingAction.Previous)
    }

    OnboardingScreenContent(
        state = state,
        onAction = viewModel::trySendAction,
        onOpenServerConfig = onOpenServerConfig,
        onOpenPrivacyPolicy = { urlLauncher.launchInApp(AppPrivacyConfig.PRIVACY_POLICY_URL) },
        onOpenTerms = { AppOrgLinks["terms"]?.let(urlLauncher::launchInApp) },
        modifier = modifier,
    )
}

/** The stateless onboarding body — what the previews and UI tests render. */
@Composable
internal fun OnboardingScreenContent(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    onOpenServerConfig: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .testTag(OnboardingTestTags.SCREEN)
            .pointerInput(state.page) {
                // A horizontal swipe is the same as Next / back and never saves.
                var dragged = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragged < -SWIPE_THRESHOLD_PX -> onAction(OnboardingAction.Next)
                            dragged > SWIPE_THRESHOLD_PX -> onAction(OnboardingAction.Previous)
                        }
                        dragged = 0f
                    },
                    onHorizontalDrag = { _, delta -> dragged += delta },
                )
            },
    ) {
        val expanded = maxWidth >= ExpandedBreakpoint
        val contentWidth = when {
            expanded -> ExpandedContentWidth
            maxWidth >= MediumBreakpoint -> MediumContentWidth
            else -> maxWidth
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .widthIn(max = contentWidth)
                .fillMaxSize()
                .padding(horizontal = sp.lg, vertical = sp.md),
            // The gap matters when the page outgrows the screen (privacy on a phone): the scrolled content
            // then starts at the top of its box and would otherwise sit flush against the brand mark.
            verticalArrangement = Arrangement.spacedBy(sp.lg),
        ) {
            OnboardingTopBar(page = state.page, onSkip = { onAction(OnboardingAction.Skip) })
            // Only the page content moves: shared-axis X, forward or back by page order (core-base/ui MOTION.md).
            val motion = MaterialTheme.motion
            AnimatedContent(
                targetState = state.page,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        KptSharedAxis.enterForward(motion) togetherWith KptSharedAxis.exitForward(motion)
                    } else {
                        KptSharedAxis.enterBack(motion) togetherWith KptSharedAxis.exitBack(motion)
                    }
                },
                modifier = Modifier.weight(1f).fillMaxWidth(),
                label = "onboarding-page",
            ) { page ->
                // The outgoing page keeps rendering its own content while it slides away.
                val pageState = state.copy(page = page)
                Box(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center,
                ) {
                    if (expanded) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(sp.xxl),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            PageIllustration(page = page, modifier = Modifier.weight(1f))
                            PageBody(
                                state = pageState,
                                onAction = onAction,
                                onOpenServerConfig = onOpenServerConfig,
                                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                                onOpenTerms = onOpenTerms,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(sp.xl)) {
                            // Compact keeps the privacy page text-first: the switches must fit above the buttons.
                            if (page != OnboardingPage.Privacy) PageIllustration(page = page)
                            PageBody(
                                state = pageState,
                                onAction = onAction,
                                onOpenServerConfig = onOpenServerConfig,
                                onOpenPrivacyPolicy = onOpenPrivacyPolicy,
                                onOpenTerms = onOpenTerms,
                            )
                        }
                    }
                }
            }
            // Pinned to the bottom and still while pages slide; on expanded it sits under the text pane.
            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(sp.xxl)) {
                    Spacer(Modifier.weight(1f))
                    PageActions(state = state, onAction = onAction, modifier = Modifier.weight(1f))
                }
            } else {
                PageActions(state = state, onAction = onAction)
            }
        }
    }
}

/**
 * The bottom bar: page dots, then Next on the intro pages or the two finishing buttons on the privacy page. The
 * buttons crossfade when the privacy page swaps them in.
 */
@Composable
internal fun PageActions(state: OnboardingState, onAction: (OnboardingAction) -> Unit, modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    val motion = MaterialTheme.motion
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(sp.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PageIndicator(page = state.page)
        AnimatedContent(
            targetState = state.page == OnboardingPage.Privacy,
            transitionSpec = {
                fadeIn(tween(motion.durationMedium1)) togetherWith fadeOut(tween(motion.durationMedium1))
            },
            label = "onboarding-actions",
        ) { privacy ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(sp.md),
            ) {
                if (privacy) {
                    if (state.saveFailed) {
                        Text(
                            text = stringResource(Res.string.feature_onboarding_save_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag(OnboardingTestTags.SAVE_FAILED),
                        )
                    }
                    Button(
                        onClick = { onAction(OnboardingAction.AllowAll) },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.ALLOW_GET_STARTED),
                    ) {
                        Text(stringResource(Res.string.feature_onboarding_allow_get_started))
                    }
                    // Equally visible, never buried: refusing must be as easy as agreeing.
                    OutlinedButton(
                        onClick = { onAction(OnboardingAction.ContinueWithChoices) },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.CONTINUE_CHOICES),
                    ) {
                        Text(
                            stringResource(
                                if (state.anyShared) {
                                    Res.string.feature_onboarding_continue_choices
                                } else {
                                    Res.string.feature_onboarding_continue_without
                                },
                            ),
                        )
                    }
                } else {
                    Button(
                        onClick = { onAction(OnboardingAction.Next) },
                        modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.NEXT),
                    ) {
                        Text(stringResource(Res.string.feature_onboarding_next))
                    }
                }
            }
        }
    }
}

/** The Mifos X mark and, on the intro pages, Skip. */
@Composable
internal fun OnboardingTopBar(page: OnboardingPage, onSkip: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        // Skip's 48dp touch target sets the bar height; keep it on the privacy page too, or the mark jumps up.
        modifier = modifier.fillMaxWidth().heightIn(min = MaterialTheme.spacing.xxxl),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.onboarding_brand_mark),
            contentDescription = stringResource(Res.string.feature_onboarding_brand_a11y),
            // 32dp: a quiet mark, not a header — 48dp competed with the page title on a phone.
            modifier = Modifier.size(MaterialTheme.spacing.xxl).testTag(OnboardingTestTags.BRAND_MARK),
        )
        if (page != OnboardingPage.Privacy) {
            val skipA11y = stringResource(Res.string.feature_onboarding_skip_a11y)
            TextButton(
                onClick = onSkip,
                modifier = Modifier
                    .testTag(OnboardingTestTags.SKIP)
                    .semantics { contentDescription = skipA11y },
            ) {
                Text(stringResource(Res.string.feature_onboarding_skip))
            }
        }
    }
}

/** The page's illustration in a tinted rounded card. Decorative — the title and body carry the meaning. */
@Composable
internal fun PageIllustration(page: OnboardingPage, modifier: Modifier = Modifier) {
    // Per-page panel tint from the Stitch renders (the highest design tier): collections amber, offline emerald.
    val panel = when (page) {
        OnboardingPage.Collections -> MaterialTheme.colorScheme.tertiaryContainer
        OnboardingPage.Offline -> MaterialTheme.colorScheme.secondaryContainer
        OnboardingPage.Clients, OnboardingPage.Privacy -> MaterialTheme.colorScheme.primaryContainer
    }
    Surface(
        color = panel,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth(),
    ) {
        Image(
            painter = painterResource(page.illustration()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ILLUSTRATION_ASPECT)
                .clip(MaterialTheme.shapes.extraLarge),
        )
    }
}

@Composable
internal fun PageBody(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    onOpenServerConfig: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.page == OnboardingPage.Privacy) {
        PrivacyPage(
            state = state,
            onAction = onAction,
            onOpenServerConfig = onOpenServerConfig,
            onOpenPrivacyPolicy = onOpenPrivacyPolicy,
            onOpenTerms = onOpenTerms,
            modifier = modifier,
        )
    } else {
        IntroPage(page = state.page, modifier = modifier)
    }
}

/** One of the three intro pages: title and body (the dots and Next live in [PageActions]). */
@Composable
internal fun IntroPage(page: OnboardingPage, modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    val (title, body) = page.copyText()
    // Title + body announce as one heading-led group when the page settles.
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(sp.md),
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag(OnboardingTestTags.TITLE).semantics { heading() },
        )
        Text(
            text = stringResource(body),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(OnboardingTestTags.BODY),
        )
    }
}

/** The privacy page: the two consent switches and the agreement line (the finishing buttons are in [PageActions]). */
@Composable
internal fun PrivacyPage(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    onOpenServerConfig: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(sp.lg)) {
        Text(
            text = stringResource(Res.string.feature_onboarding_privacy_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.testTag(OnboardingTestTags.PRIVACY_TITLE).semantics { heading() },
        )
        Text(
            text = stringResource(Res.string.feature_onboarding_privacy_lead),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            OnboardingConsentRow(
                title = stringResource(Res.string.feature_onboarding_analytics_label),
                description = stringResource(Res.string.feature_onboarding_analytics_body),
                checked = state.analytics,
                onCheckedChange = { onAction(OnboardingAction.SetAnalytics(it)) },
                modifier = Modifier.testTag(OnboardingTestTags.ANALYTICS_SWITCH),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OnboardingConsentRow(
                title = stringResource(Res.string.feature_onboarding_crash_label),
                description = stringResource(Res.string.feature_onboarding_crash_body),
                checked = state.crashReports,
                onCheckedChange = { onAction(OnboardingAction.SetCrashReports(it)) },
                modifier = Modifier.testTag(OnboardingTestTags.CRASH_REPORTS_SWITCH),
            )
        }
        NoClientDataNote()
        AgreementText(onOpenPrivacyPolicy = onOpenPrivacyPolicy, onOpenTerms = onOpenTerms)
        Text(
            text = stringResource(Res.string.feature_onboarding_settings_later),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(
            onClick = onOpenServerConfig,
            modifier = Modifier.testTag(OnboardingTestTags.CONNECT_INSTANCE),
        ) {
            Text(stringResource(Res.string.feature_onboarding_connect_instance))
        }
    }
}

/**
 * One consent: label and what it sends, with a switch. The whole row toggles with the switch role, so it is one
 * large touch target and reads as one control to a screen reader (FEATURE_LAYER "Settings sub-surface shape").
 */
@Composable
internal fun OnboardingConsentRow(
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
            .padding(vertical = sp.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(sp.md),
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(sp.xs)) {
            // Explicit: there is no Surface behind this row, so the inherited content colour would be wrong in dark
            // theme (the first golden rendered these titles dark-on-dark).
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
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

/** "Nothing about your clients, loans or savings is ever sent." */
@Composable
internal fun NoClientDataNote(modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(sp.lg),
            horizontalArrangement = Arrangement.spacedBy(sp.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = AppIcons.ShielOutlined,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = stringResource(Res.string.feature_onboarding_no_client_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/** "By using our service you accept our Terms & Conditions and Privacy Policy." — both as in-app links. */
@Composable
internal fun AgreementText(onOpenPrivacyPolicy: () -> Unit, onOpenTerms: () -> Unit, modifier: Modifier = Modifier) {
    val policy = stringResource(Res.string.feature_onboarding_privacy_policy)
    val terms = stringResource(Res.string.feature_onboarding_terms)
    val sentence = stringResource(Res.string.feature_onboarding_agreement, terms, policy)
    val linkStyle = TextLinkStyles(
        style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline),
    )
    val text = buildAnnotatedString {
        // Locate each link inside the LOCALIZED sentence, so a locale that orders them differently still links
        // the right words.
        var cursor = 0
        listOf(policy to onOpenPrivacyPolicy, terms to onOpenTerms)
            .map { (label, open) -> Triple(sentence.indexOf(label), label, open) }
            .filter { it.first >= 0 }
            .sortedBy { it.first }
            .forEach { (start, label, open) ->
                append(sentence.substring(cursor, start))
                withLink(LinkAnnotation.Clickable(tag = label, styles = linkStyle) { open() }) { append(label) }
                cursor = start + label.length
            }
        append(sentence.substring(cursor))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.testTag(OnboardingTestTags.AGREEMENT),
    )
}

/** Four dots, the current page drawn as a wide pill. */
@Composable
internal fun PageIndicator(page: OnboardingPage, modifier: Modifier = Modifier) {
    val sp = MaterialTheme.spacing
    val total = OnboardingPage.entries.size
    val description = stringResource(Res.string.feature_onboarding_page_of, page.ordinal + 1, total)
    Row(
        modifier = modifier
            .testTag(OnboardingTestTags.PAGE_INDICATOR)
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(sp.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OnboardingPage.entries.forEach { dot ->
            val current = dot == page
            Box(
                modifier = Modifier
                    .height(sp.sm)
                    .width(if (current) sp.xl else sp.sm)
                    .clip(CircleShape)
                    .background(
                        if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}

private fun OnboardingPage.illustration(): DrawableResource = when (this) {
    OnboardingPage.Clients -> Res.drawable.onboarding_clients_illustration
    OnboardingPage.Collections -> Res.drawable.onboarding_collections_illustration
    OnboardingPage.Offline -> Res.drawable.onboarding_offline_illustration
    OnboardingPage.Privacy -> Res.drawable.onboarding_privacy_illustration
}

private fun OnboardingPage.copyText(): Pair<StringResource, StringResource> = when (this) {
    OnboardingPage.Clients -> Res.string.feature_onboarding_clients_title to Res.string.feature_onboarding_clients_body
    OnboardingPage.Collections ->
        Res.string.feature_onboarding_collections_title to Res.string.feature_onboarding_collections_body
    OnboardingPage.Offline -> Res.string.feature_onboarding_offline_title to Res.string.feature_onboarding_offline_body
    // The privacy page renders its own layout (PrivacyPage); never reached.
    OnboardingPage.Privacy ->
        Res.string.feature_onboarding_privacy_title to Res.string.feature_onboarding_privacy_lead
}

/** The generated illustrations are 4:3 (Stitch-declared 800x600). */
private const val ILLUSTRATION_ASPECT = 4f / 3f

/** A drag shorter than this is a tap or a scroll, not a page swipe. */
private const val SWIPE_THRESHOLD_PX = 120f
