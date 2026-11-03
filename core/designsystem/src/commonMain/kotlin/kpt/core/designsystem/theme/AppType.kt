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

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import kpt.core.designsystem.generated.resources.Res
import kpt.core.designsystem.generated.resources.inter_black
import kpt.core.designsystem.generated.resources.inter_bold
import kpt.core.designsystem.generated.resources.inter_extra_bold
import kpt.core.designsystem.generated.resources.inter_extra_light
import kpt.core.designsystem.generated.resources.inter_light
import kpt.core.designsystem.generated.resources.inter_medium
import kpt.core.designsystem.generated.resources.inter_regular
import kpt.core.designsystem.generated.resources.inter_semi_bold
import kpt.core.designsystem.generated.resources.inter_thin
import org.jetbrains.compose.resources.Font

@Composable
private fun fontFamily(): FontFamily {
    return FontFamily(
        Font(Res.font.inter_black, FontWeight.Black),
        Font(Res.font.inter_bold, FontWeight.Bold),
        Font(Res.font.inter_semi_bold, FontWeight.SemiBold),
        Font(Res.font.inter_medium, FontWeight.Medium),
        Font(Res.font.inter_regular, FontWeight.Normal),
        Font(Res.font.inter_light, FontWeight.Light),
        Font(Res.font.inter_thin, FontWeight.Thin),
        Font(Res.font.inter_extra_light, FontWeight.ExtraLight),
        Font(Res.font.inter_extra_bold, FontWeight.ExtraBold),
    )
}

// Set of Material typography styles to start with
@Composable
internal fun mifosTypography() = Typography(
    displayLarge = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.None,
        ),
    ),
    titleLarge = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Default text style
    bodyLarge = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.None,
        ),
    ),
    bodyMedium = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    // Used for Button
    labelLarge = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // Used for Navigation items
    labelMedium = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    ),
    // Used for Tag
    labelSmall = TextStyle(
        fontFamily = fontFamily(),
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.LastLineBottom,
        ),
    ),
)

/** The app's text styles, in the app font. */
object AppTypography {
    /** Material display large text style. */
    val displayLarge: TextStyle
        @Composable get() = TextStyle(
            fontSize = 57.sp,
            lineHeight = 64.sp,
            fontFamily = fontFamily(),
            letterSpacing = (-0.25).sp,
            fontWeight = FontWeight.Normal,
        )

    /** Material display medium text style. */
    val displayMedium: TextStyle
        @Composable get() = TextStyle(
            fontSize = 45.sp,
            lineHeight = 52.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight.Normal,
        )

    /** Material display small text style. */
    val displaySmall: TextStyle
        @Composable get() = TextStyle(
            fontSize = 36.sp,
            lineHeight = 44.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(700),
        )

    /** Material headline large text style. */
    val headlineLarge: TextStyle
        @Composable get() = TextStyle(
            fontSize = 32.sp,
            lineHeight = 40.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(600),
        )

    // verified
    /** Material headline medium text style. */
    val headlineMedium: TextStyle
        @Composable get() = TextStyle(
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(700),
        )

    // verified
    /** Material headline small text style. */
    val headlineSmall: TextStyle
        @Composable get() = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(400),
        )

    // verified
    /** Material headline small text style, emphasized (heavier weight). */
    val headlineSmallEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(700),
        )

    // verified
    /** Material title large text style. */
    val titleLarge: TextStyle
        @Composable get() = TextStyle(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(700),
        )

    /** Material title large text style, emphasized (heavier weight). */
    val titleLargeEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(500),
        )

    // verified
    /** Material title medium text style. */
    val titleMedium: TextStyle
        @Composable get() = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(500),
        )

    /** Material title medium text style, emphasized (heavier weight). */
    val titleMediumEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(600),
        )

    /** Material title small text style. */
    val titleSmall: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(400),
        )

    // verified
    /** Material title small text style, emphasized (heavier weight). */
    val titleSmallEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(600),
        )

    /** Material label large text style. */
    val labelLarge: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.1.sp,
            fontWeight = FontWeight(500),
        )

    /** Material label large text style, emphasized (heavier weight). */
    val labelLargeEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.1.sp,
            fontWeight = FontWeight(600),
        )

    // verified
    /** Material label medium text style. */
    val labelMedium: TextStyle
        @Composable get() = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight(500),
        )

    /** Material label medium text style, emphasized (heavier weight). */
    val labelMediumEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight(600),
        )

    /** Material label small text style. */
    val labelSmall: TextStyle
        @Composable get() = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight(500),
        )

    /** Material label small text style, emphasized (heavier weight). */
    val labelSmallEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 11.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight(600),
        )

    // verified
    /** Material body large text style. */
    val bodyLarge: TextStyle
        @Composable get() = TextStyle(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.5.sp,
            fontWeight = FontWeight(400),
        )

    /** Material body medium text style. */
    val bodyMedium: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.25.sp,
            fontWeight = FontWeight(400),
        )

    /** Material body medium text style, emphasized (heavier weight). */
    val bodyMediumEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.25.sp,
            fontWeight = FontWeight(500),
        )

    // verified
    /** Material body small text style. */
    val bodySmall: TextStyle
        @Composable get() = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight(400),
        )

    /** Material body small text style, emphasized (heavier weight). */
    val bodySmallEmphasized: TextStyle
        @Composable get() = TextStyle(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontFamily = fontFamily(),
            letterSpacing = 0.4.sp,
            fontWeight = FontWeight(500),
        )

    // verified
    /** Text style for small status tags. */
    val tag: TextStyle
        @Composable get() = TextStyle(
            fontSize = 10.sp,
            fontFamily = fontFamily(),
            fontWeight = FontWeight(600),
        )
}
