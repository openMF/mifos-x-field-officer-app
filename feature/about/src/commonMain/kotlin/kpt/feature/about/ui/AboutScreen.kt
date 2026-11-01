/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.about.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.util.ShareUtils
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.feature.about.generated.resources.Res
import kpt.feature.about.generated.resources.feature_about_community
import kpt.feature.about.generated.resources.feature_about_community_sub
import kpt.feature.about.generated.resources.feature_about_contact
import kpt.feature.about.generated.resources.feature_about_contributors
import kpt.feature.about.generated.resources.feature_about_contributors_sub
import kpt.feature.about.generated.resources.feature_about_copyright
import kpt.feature.about.generated.resources.feature_about_developed_by
import kpt.feature.about.generated.resources.feature_about_ic_source_code
import kpt.feature.about.generated.resources.feature_about_ic_website
import kpt.feature.about.generated.resources.feature_about_license
import kpt.feature.about.generated.resources.feature_about_license_sub
import kpt.feature.about.generated.resources.feature_about_logo
import kpt.feature.about.generated.resources.feature_about_logo_description
import kpt.feature.about.generated.resources.feature_about_product_site
import kpt.feature.about.generated.resources.feature_about_product_site_sub
import kpt.feature.about.generated.resources.feature_about_section_help
import kpt.feature.about.generated.resources.feature_about_section_open_source
import kpt.feature.about.generated.resources.feature_about_section_organisation
import kpt.feature.about.generated.resources.feature_about_source_code
import kpt.feature.about.generated.resources.feature_about_source_code_sub
import kpt.feature.about.generated.resources.feature_about_tagline
import kpt.feature.about.generated.resources.feature_about_title
import kpt.feature.about.generated.resources.feature_about_version
import kpt.feature.about.generated.resources.feature_about_version_hint
import kpt.feature.about.generated.resources.feature_about_website
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The app's identity page: brand header (logo, name, purpose, version), the organisation behind
 * it, its open-source home, and the copyright line. Ported from `6b66e8a43:feature/about` — see
 * [AboutViewModel] for what changed and why.
 */
@Composable
internal fun AboutScreen(
    onNavigateBack: () -> Unit,
    viewModel: AboutViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    // core-BASE's ShareUtils — the one MainActivity wires with `setActivityProvider`. core/ui has a
    // look-alike copy that nothing wires; calling it threw on the first tap (device, 2026-10-24).
    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            AboutEvent.NavigateBack -> onNavigateBack()
            AboutEvent.OpenAppInfo -> ShareUtils.openAppInfo()
            is AboutEvent.OpenUrl -> ShareUtils.openUrl(event.url)
        }
    }

    AboutContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun AboutContent(
    state: AboutState,
    onAction: (AboutAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_about_title),
        onBackPressed = { onAction(AboutAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(AboutTestTags.SCREEN),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            item { BrandHeader(state = state, onAction = onAction) }

            AboutSection.entries.forEach { section ->
                val links = state.links.filter { it.kind.section == section }
                // A section with nothing configured is not drawn at all — no orphan heading.
                if (links.isNotEmpty()) {
                    item(key = section.name) {
                        LinkSection(title = section.title(), links = links, onAction = onAction)
                    }
                }
            }

            item { Footer(state) }
        }
    }
}

@Composable
private fun BrandHeader(state: AboutState, onAction: (AboutAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md)
            .padding(top = KptTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // White in BOTH themes: the Mifos X mark's cross is transparent, so on a dark tile it read as
        // a dark cross — a different mark. Brand marks sit on the background they were drawn for.
        Surface(
            shape = DesignToken.shapes.extraLargeIncreased,
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Image(
                painter = painterResource(Res.drawable.feature_about_logo),
                contentDescription = stringResource(Res.string.feature_about_logo_description, state.appName),
                modifier = Modifier.padding(KptTheme.spacing.lg).size(LOGO_SIZE),
            )
        }
        Spacer(Modifier.height(KptTheme.spacing.md))
        Text(
            text = state.appName,
            style = KptTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = KptTheme.colorScheme.onSurface,
            modifier = Modifier.testTag(AboutTestTags.APP_NAME),
        )
        Spacer(Modifier.height(KptTheme.spacing.xs))
        Text(
            text = stringResource(Res.string.feature_about_tagline),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(KptTheme.spacing.md))
        // The original's "App Version" row opened the system app-info page; kept, as a chip.
        Surface(
            onClick = { onAction(AboutAction.VersionClicked) },
            shape = DesignToken.shapes.extraLargeIncreased,
            color = KptTheme.colorScheme.secondaryContainer,
            modifier = Modifier.testTag(AboutTestTags.VERSION),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.feature_about_version, state.versionName),
                    style = KptTheme.typography.labelLarge,
                    color = KptTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = stringResource(Res.string.feature_about_version_hint),
                    style = KptTheme.typography.labelSmall,
                    color = KptTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        if (state.orgName.isNotBlank()) {
            Spacer(Modifier.height(KptTheme.spacing.sm))
            Text(
                text = stringResource(Res.string.feature_about_developed_by, state.orgName),
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LinkSection(title: String, links: List<AboutLink>, onAction: (AboutAction) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = KptTheme.spacing.md)) {
        Text(
            text = title,
            style = KptTheme.typography.titleSmall,
            color = KptTheme.colorScheme.primary,
            modifier = Modifier.padding(start = KptTheme.spacing.xs, bottom = KptTheme.spacing.sm),
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = KptTheme.colorScheme.surfaceContainerLow),
            shape = DesignToken.shapes.medium,
        ) {
            links.forEachIndexed { index, link ->
                if (index > 0) HorizontalDivider(color = KptTheme.colorScheme.outlineVariant)
                LinkRow(link = link, onClick = { onAction(AboutAction.LinkClicked(link)) })
            }
        }
    }
}

@Composable
private fun LinkRow(link: AboutLink, onClick: () -> Unit) {
    val title = stringResource(link.kind.title())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(KptTheme.spacing.md)
            .testTag(AboutTestTags.link(link.kind)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = link.kind.icon(),
            // Decorative: the title beside it already names the row.
            contentDescription = null,
            tint = KptTheme.colorScheme.primary,
            modifier = Modifier.size(ICON_SIZE),
        )
        Column(modifier = Modifier.weight(1f).padding(start = KptTheme.spacing.md)) {
            Text(text = title, style = KptTheme.typography.bodyLarge, color = KptTheme.colorScheme.onSurface)
            val detail = link.detail ?: link.kind.defaultDetail()?.let { stringResource(it) }
            detail?.let {
                Text(text = it, style = KptTheme.typography.bodySmall, color = KptTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = KptTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Footer(state: AboutState) {
    if (state.orgName.isBlank()) return
    Text(
        text = stringResource(Res.string.feature_about_copyright, state.copyrightYear, state.orgName),
        style = KptTheme.typography.labelMedium,
        color = KptTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.lg)
            .testTag(AboutTestTags.COPYRIGHT),
    )
}

@Composable
private fun AboutSection.title(): String = stringResource(
    when (this) {
        AboutSection.HELP -> Res.string.feature_about_section_help
        AboutSection.ORGANISATION -> Res.string.feature_about_section_organisation
        AboutSection.OPEN_SOURCE -> Res.string.feature_about_section_open_source
    },
)

private fun AboutLinkKind.title(): StringResource = when (this) {
    AboutLinkKind.PRODUCT_SITE -> Res.string.feature_about_product_site
    AboutLinkKind.COMMUNITY -> Res.string.feature_about_community
    AboutLinkKind.WEBSITE -> Res.string.feature_about_website
    AboutLinkKind.CONTACT -> Res.string.feature_about_contact
    AboutLinkKind.SOURCE_CODE -> Res.string.feature_about_source_code
    AboutLinkKind.CONTRIBUTORS -> Res.string.feature_about_contributors
    AboutLinkKind.LICENSE -> Res.string.feature_about_license
}

/** The subtitle for rows whose link carries none of its own (website/contact show the address). */
private fun AboutLinkKind.defaultDetail(): StringResource? = when (this) {
    AboutLinkKind.PRODUCT_SITE -> Res.string.feature_about_product_site_sub
    AboutLinkKind.COMMUNITY -> Res.string.feature_about_community_sub
    AboutLinkKind.SOURCE_CODE -> Res.string.feature_about_source_code_sub
    AboutLinkKind.CONTRIBUTORS -> Res.string.feature_about_contributors_sub
    AboutLinkKind.LICENSE -> Res.string.feature_about_license_sub
    AboutLinkKind.WEBSITE, AboutLinkKind.CONTACT -> null
}

@Composable
private fun AboutLinkKind.icon(): Painter = when (this) {
    // The original's own glyphs for these two.
    AboutLinkKind.WEBSITE -> painterResource(Res.drawable.feature_about_ic_website)
    AboutLinkKind.SOURCE_CODE -> painterResource(Res.drawable.feature_about_ic_source_code)
    AboutLinkKind.PRODUCT_SITE -> rememberVectorPainter(AppIcons.MobileFriendly)
    AboutLinkKind.COMMUNITY -> rememberVectorPainter(AppIcons.Groups)
    AboutLinkKind.CONTACT -> rememberVectorPainter(AppIcons.Link)
    AboutLinkKind.CONTRIBUTORS -> rememberVectorPainter(AppIcons.PersonOutline)
    AboutLinkKind.LICENSE -> rememberVectorPainter(AppIcons.ShielOutlined)
}

private val LOGO_SIZE = 88.dp
private val ICON_SIZE = 24.dp

/** Stable handles for UI tests and device flows. */
internal object AboutTestTags {
    const val SCREEN = "about:screen"
    const val APP_NAME = "about:appName"
    const val VERSION = "about:version"
    const val COPYRIGHT = "about:copyright"

    fun link(kind: AboutLinkKind) = "about:link:${kind.name.lowercase()}"
}
