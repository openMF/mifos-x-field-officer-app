/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * The app's MD3 palette, generated from the project design-token SoT (`design-tokens.yaml`) by
 * `kmp-designsystem-gen --project-theme` (RULE-IMPL-DESIGN-TOKEN-PARITY-001): Trust Indigo primary,
 * Emerald secondary, Warm Amber tertiary on a near-white slate surface — the palette every Stitch
 * mockup is rendered in.
 *
 * Brand decision 2026-10-27 (operator): the Stitch / design-token palette IS the brand. This replaces
 * the 2026-10-22 Mifos-blue port (c79850800). The tokens declare the light scheme only; the dark
 * scheme is the indigo set the tokens were extracted alongside.
 *
 * `KptTheme` builds `lightScheme`/`darkScheme` from these names, so the values re-theme every
 * surface at once. Change a value in design-tokens.yaml and regenerate — never here by hand.
 */

// ── Light scheme ─────────────────────────────────────────────────────────────
// Primary — Trust Indigo.
val primaryLight = Color(0xFF4338CA)

/** Material `onPrimary` colour role, light scheme. */
val onPrimaryLight = Color(0xFFFFFFFF)

/** Material `primaryContainer` colour role, light scheme. */
val primaryContainerLight = Color(0xFFE0E7FF)

/** Material `onPrimaryContainer` colour role, light scheme. */
val onPrimaryContainerLight = Color(0xFF1F1D75)

// Secondary — Emerald.
/** Material `secondary` colour role, light scheme. */
val secondaryLight = Color(0xFF059669)

/** Material `onSecondary` colour role, light scheme. */
val onSecondaryLight = Color(0xFFFFFFFF)

/** Material `secondaryContainer` colour role, light scheme. */
val secondaryContainerLight = Color(0xFFD1FAE5)

/** Material `onSecondaryContainer` colour role, light scheme. */
val onSecondaryContainerLight = Color(0xFF064E3B)

// Tertiary — Warm Amber.
/** Material `tertiary` colour role, light scheme. */
val tertiaryLight = Color(0xFFD97706)

/** Material `onTertiary` colour role, light scheme. */
val onTertiaryLight = Color(0xFFFFFFFF)

/** Material `tertiaryContainer` colour role, light scheme. */
val tertiaryContainerLight = Color(0xFFFEF3C7)

/** Material `onTertiaryContainer` colour role, light scheme. */
val onTertiaryContainerLight = Color(0xFF78350F)

// Error.
/** Material `error` colour role, light scheme. */
val errorLight = Color(0xFFF87171)

/** Material `onError` colour role, light scheme. */
val onErrorLight = Color(0xFFFFFFFF)

/** Material `errorContainer` colour role, light scheme. */
val errorContainerLight = Color(0xFFFFE4E1)

/** Material `onErrorContainer` colour role, light scheme. */
val onErrorContainerLight = Color(0xFF9F1239)

// Background + surface — slate neutral hierarchy.
/** Material `background` colour role, light scheme. */
val backgroundLight = Color(0xFFFAFAFB)

/** Material `onBackground` colour role, light scheme. */
val onBackgroundLight = Color(0xFF0F172A)

/** Material `surface` colour role, light scheme. */
val surfaceLight = Color(0xFFFAFAFB)

/** Material `onSurface` colour role, light scheme. */
val onSurfaceLight = Color(0xFF0F172A)

/** Material `surfaceVariant` colour role, light scheme. */
val surfaceVariantLight = Color(0xFFE2E8F0)

/** Material `onSurfaceVariant` colour role, light scheme. */
val onSurfaceVariantLight = Color(0xFF475569)

/** Material `outline` colour role, light scheme. */
val outlineLight = Color(0xFF94A3B8)

/** Material `outlineVariant` colour role, light scheme. */
val outlineVariantLight = Color(0xFFCBD5E1)

/** Material `scrim` colour role, light scheme. */
val scrimLight = Color(0xFF000000)

/** Material `inverseSurface` colour role, light scheme. */
val inverseSurfaceLight = Color(0xFF1E293B)

/** Material `inverseOnSurface` colour role, light scheme. */
val inverseOnSurfaceLight = Color(0xFFF1F5F9)

/** Material `inversePrimary` colour role, light scheme. */
val inversePrimaryLight = Color(0xFFA5B4FC)

// Surface tonal ladder — used by AppCard / HeroCard to feel lifted.
/** Material `surfaceDim` colour role, light scheme. */
val surfaceDimLight = Color(0xFFE2E8F0)

/** Material `surfaceBright` colour role, light scheme. */
val surfaceBrightLight = Color(0xFFFAFAFB)

/** Material `surfaceContainerLowest` colour role, light scheme. */
val surfaceContainerLowestLight = Color(0xFFFFFFFF)

/** Material `surfaceContainerLow` colour role, light scheme. */
val surfaceContainerLowLight = Color(0xFFF8FAFC)

/** Material `surfaceContainer` colour role, light scheme. */
val surfaceContainerLight = Color(0xFFF1F5F9)

/** Material `surfaceContainerHigh` colour role, light scheme. */
val surfaceContainerHighLight = Color(0xFFE2E8F0)

/** Material `surfaceContainerHighest` colour role, light scheme. */
val surfaceContainerHighestLight = Color(0xFFCBD5E1)

// ── Dark scheme ──────────────────────────────────────────────────────────────
/** Material `primary` colour role, dark scheme. */
val primaryDark = Color(0xFFA5B4FC)

/** Material `onPrimary` colour role, dark scheme. */
val onPrimaryDark = Color(0xFF1F1D75)

/** Material `primaryContainer` colour role, dark scheme. */
val primaryContainerDark = Color(0xFF3730A3)

/** Material `onPrimaryContainer` colour role, dark scheme. */
val onPrimaryContainerDark = Color(0xFFE0E7FF)

/** Material `secondary` colour role, dark scheme. */
val secondaryDark = Color(0xFF6EE7B7)

/** Material `onSecondary` colour role, dark scheme. */
val onSecondaryDark = Color(0xFF064E3B)

/** Material `secondaryContainer` colour role, dark scheme. */
val secondaryContainerDark = Color(0xFF065F46)

/** Material `onSecondaryContainer` colour role, dark scheme. */
val onSecondaryContainerDark = Color(0xFFD1FAE5)

/** Material `tertiary` colour role, dark scheme. */
val tertiaryDark = Color(0xFFFCD34D)

/** Material `onTertiary` colour role, dark scheme. */
val onTertiaryDark = Color(0xFF78350F)

/** Material `tertiaryContainer` colour role, dark scheme. */
val tertiaryContainerDark = Color(0xFF92400E)

/** Material `onTertiaryContainer` colour role, dark scheme. */
val onTertiaryContainerDark = Color(0xFFFEF3C7)

/** Material `error` colour role, dark scheme. */
val errorDark = Color(0xFFFDBA74)

/** Material `onError` colour role, dark scheme. */
val onErrorDark = Color(0xFF7C2D12)

/** Material `errorContainer` colour role, dark scheme. */
val errorContainerDark = Color(0xFFC2410C)

/** Material `onErrorContainer` colour role, dark scheme. */
val onErrorContainerDark = Color(0xFFFFEDD5)

/** Material `background` colour role, dark scheme. */
val backgroundDark = Color(0xFF0F172A)

/** Material `onBackground` colour role, dark scheme. */
val onBackgroundDark = Color(0xFFF1F5F9)

/** Material `surface` colour role, dark scheme. */
val surfaceDark = Color(0xFF0F172A)

/** Material `onSurface` colour role, dark scheme. */
val onSurfaceDark = Color(0xFFF1F5F9)

/** Material `surfaceVariant` colour role, dark scheme. */
val surfaceVariantDark = Color(0xFF1E293B)

/** Material `onSurfaceVariant` colour role, dark scheme. */
val onSurfaceVariantDark = Color(0xFFCBD5E1)

/** Material `outline` colour role, dark scheme. */
val outlineDark = Color(0xFF64748B)

/** Material `outlineVariant` colour role, dark scheme. */
val outlineVariantDark = Color(0xFF334155)

/** Material `scrim` colour role, dark scheme. */
val scrimDark = Color(0xFF000000)

/** Material `inverseSurface` colour role, dark scheme. */
val inverseSurfaceDark = Color(0xFFF1F5F9)

/** Material `inverseOnSurface` colour role, dark scheme. */
val inverseOnSurfaceDark = Color(0xFF1E293B)

/** Material `inversePrimary` colour role, dark scheme. */
val inversePrimaryDark = Color(0xFF4338CA)

/** Material `surfaceDim` colour role, dark scheme. */
val surfaceDimDark = Color(0xFF0F172A)

/** Material `surfaceBright` colour role, dark scheme. */
val surfaceBrightDark = Color(0xFF374558)

/** Material `surfaceContainerLowest` colour role, dark scheme. */
val surfaceContainerLowestDark = Color(0xFF020617)

/** Material `surfaceContainerLow` colour role, dark scheme. */
val surfaceContainerLowDark = Color(0xFF1E293B)

/** Material `surfaceContainer` colour role, dark scheme. */
val surfaceContainerDark = Color(0xFF243044)

/** Material `surfaceContainerHigh` colour role, dark scheme. */
val surfaceContainerHighDark = Color(0xFF2D3B52)

/** Material `surfaceContainerHighest` colour role, dark scheme. */
val surfaceContainerHighestDark = Color(0xFF374558)
