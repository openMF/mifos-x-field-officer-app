/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import kpt.core.base.designsystem.KptMaterialTheme
import kpt.core.base.designsystem.theme.KptThemeProviderImpl
import kpt.core.base.designsystem.toKptColorScheme
import kpt.core.base.designsystem.toKptTypography
/**
 * Applies the app's colours and typography: the platform scheme when dynamic theming is on, else the brand scheme.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    androidTheme: Boolean = false,
    shouldDisplayDynamicTheming: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        shouldDisplayDynamicTheming -> colorScheme(darkTheme, true)
        androidTheme -> if (darkTheme) darkColorScheme() else lightColorScheme()
        else -> if (darkTheme) darkScheme else lightScheme
    }.toKptColorScheme()

    val typography = mifosTypography().toKptTypography()

    val theme = KptThemeProviderImpl(
        colors = colorScheme,
        typography = typography,
    )

    KptMaterialTheme(
        theme = theme,
        content = content,
    )
}

/** The platform's colour scheme: dynamic colour where the platform has it, else the brand scheme. */
@Composable
expect fun colorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme
