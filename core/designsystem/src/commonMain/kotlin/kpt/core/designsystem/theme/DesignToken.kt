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

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The current design tokens, read from the composition. */
object DesignToken {
    /** The current spacing tokens. */
    val spacing: AppSpacing
        @Composable
        get() = LocalAppSpacing.current

    /** The current padding tokens. */
    val padding: AppPadding
        @Composable
        get() = LocalPadding.current

    /** The current shape tokens. */
    val shapes: AppShapes
        @Composable
        get() = LocalShapes.current

    /** The current elevation tokens. */
    val elevation: AppElevation
        @Composable
        get() = LocalAppElevation.current

    /** The current size tokens. */
    val sizes: AppSizes
        @Composable
        get() = LocalSizes.current

    /** The current stroke-width tokens. */
    val strokes: AppStrokes
        @Composable
        get() = LocalStrokes.current
}

/**
 * Defines standardized spacing values used throughout the application for
 * consistent layout spacing.
 *
 * This immutable data class provides a comprehensive set of spacing values
 * that should be used for margins, gaps between elements, and other layout
 * spacing requirements.
 *
 * Usage example:
 * ```
 * Column(
 *     verticalArrangement = Arrangement.spacedBy(DesignTokens.spacing.medium)
 * ) {
 *     Text("First item")
 *     Text("Second item")
 * }
 * ```
 *
 * @param none Zero spacing for elements that should have no gap (0.dp).
 * @param extraSmall Minimal spacing for very tight layouts or dense UI areas (4.dp).
 * @param small Small spacing typically used for compact components (8.dp).
 * @param medium Moderate spacing used for standard component separation (12.dp).
 * @param large Large spacing suitable for well-spaced layout blocks (16.dp).
 * @param largeIncreased Slightly larger than `large` for extra breathing room (20.dp).
 * @param extraLarge Extra spacing for visual emphasis between major sections (28.dp).
 * @param extraLargeIncreased Even more prominent spacing, useful in scrollable or modal layouts (32.dp).
 * @param extraExtraLarge Significant spacing for major layout divisions or empty states (48.dp).
 * @param full Maximum spacing, typically used for centering or deep offset layouts (1000.dp).
 */
@Immutable
data class AppSpacing(
    /** Medium small spacing: 6 dp. */
    val mediumSmall: Dp = 6.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val largeIncreased: Dp = 20.dp,
    val extraLarge: Dp = 28.dp,
    /** 1 dp of spacing. */
    val dp1: Dp = 1.dp,
    /** 5 dp of spacing. */
    val dp5: Dp = 5.dp,
    /** 18 dp of spacing. */
    val dp18: Dp = 18.dp,
    /** 44 dp of spacing. */
    val dp44: Dp = 44.dp,
    /** 46 dp of spacing. */
    val dp46: Dp = 46.dp,
)

/**
 * Defines standardized padding values for consistent internal spacing
 * within components.
 *
 * This immutable data class provides padding values that should be used
 * for internal component spacing, content padding, and other internal
 * layout requirements. These values are optimized for touch targets and
 * readability.
 *
 * Usage example:
 * ```
 * Card(
 *     modifier = Modifier.padding(DesignTokens.padding.card)
 * ) {
 *     Text(
 *         text = "Content",
 *         modifier = Modifier.padding(DesignTokens.padding.content)
 *     )
 * }
 * ```
 *
 * @param none Zero padding for elements that should have no internal spacing (0.dp).
 * @param extraSmall Very minimal padding for extremely compact UI elements (4.dp).
 * @param small Small padding suitable for compact components (8.dp).
 * @param medium Moderate padding for standard components (12.dp).
 * @param large Generous padding used in card or section layouts (16.dp).
 * @param largeIncreased Slightly more than large for edge cases requiring more spacing (20.dp).
 * @param extraLarge Larger padding for major containers or elevated surfaces (28.dp).
 * @param extraLargeIncreased Even more prominent padding for large UI blocks (32.dp).
 * @property extraExtraLarge Very large padding for full-width layouts or modal spacing (48.dp).
 * @param full Extremely large padding for special layout use-cases like centering or offset (1000.dp).
 */

@Immutable
data class AppPadding(
    val none: Dp = 0.dp,
    /** Extra extra small padding: 2 dp. */
    val extraExtraSmall: Dp = 2.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 12.dp,
    val large: Dp = 16.dp,
    val largeIncreased: Dp = 20.dp,
    /** Large increased extra padding: 24 dp. */
    val largeIncreasedExtra: Dp = 24.dp,
    val extraLarge: Dp = 28.dp,
    val extraExtraLarge: Dp = 48.dp,
    /** 1 dp of padding. */
    val dp1: Dp = 1.dp,
    /** 6 dp of padding. */
    val dp6: Dp = 6.dp,
    /** 18 dp of padding. */
    val dp18: Dp = 18.dp,
    /** 56 dp of padding. */
    val dp56: Dp = 56.dp,
)

/**
 * Defines standardized shape values for consistent corner radii and border
 * shapes across components.
 *
 * This immutable data class provides shape definitions that ensure visual
 * consistency across all UI components. These shapes follow Material
 * Design principles while maintaining the application's visual identity.
 *
 * Usage example:
 * ```
 * Card(
 *     shape = DesignTokens.shapes.card
 * ) {
 *     // Card content
 * }
 *
 * Button(
 *     shape = DesignTokens.shapes.button,
 *     onClick = { }
 * ) {
 *     Text("Button")
 * }
 * ```
 *
 * @param none No corner radius; results in sharp rectangular shapes (0.dp).
 * @param extraSmall Extra small corner radius for minimal rounding (4.dp).
 * @v small Small corner radius for subtle rounded corners (8.dp).
 * @param medium Medium corner radius for standard UI components (12.dp).
 * @param large Large corner radius for more pronounced rounding (16.dp).
 * @param largeIncreased Slightly larger than large for prominent components (20.dp).
 * @param extraLarge Extra large corner radius for highly rounded UI elements (28.dp).
 * @param extraLargeIncreased Larger extra-large radius for rounded designs (32.dp).
 * @param extraExtraLarge Extremely large radius for pill-shaped or very soft corners (48.dp).
 * @param full Fully rounded corners for shapes like capsules (1000.dp).
 * @param circle Perfectly circular shape (50% radius of height/width).
 * @param bottomSheet Shape for bottom sheets with only top corners rounded (16.dp topStart, topEnd).
 * @param topBar Shape for top bars with only bottom corners rounded (16.dp bottomStart, bottomEnd).
 */
@Immutable
data class AppShapes(
    val none: Shape = RoundedCornerShape(0.dp),
    /** Rounded shape, corners 8 dp. */
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val extraLargeIncreased: Shape = RoundedCornerShape(32.dp),
    val full: Shape = RoundedCornerShape(1000.dp),
    /** Rounded shape, corners top start 12, top end 12 dp. */
    val topMedium: Shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
)

/**
 * Defines standardized elevation values for consistent shadow depths and
 * layering across components.
 *
 * This immutable data class provides elevation values that create a
 * consistent visual hierarchy and depth perception throughout the
 * application. These values follow Material Design elevation guidelines
 * to ensure appropriate shadow depths for different component types.
 *
 * Usage example:
 * ```
 * Card(
 *     elevation = CardDefaults.cardElevation(
 *         defaultElevation = DesignTokens.elevation.card
 *     )
 * ) {
 *     // Card content
 * }
 *
 * FloatingActionButton(
 *     elevation = FloatingActionButtonDefaults.elevation(
 *         defaultElevation = DesignTokens.elevation.fab
 *     ),
 *     onClick = { }
 * ) {
 *     Icon(Icons.Default.Add, contentDescription = "Add")
 * }
 * ```
 *
 * @param none No elevation for flat components at surface level
 * @param elevation Dialog elevation for maximum prominence (25dp)
 * @param appBar Elevation for top app bar (2dp)
 */
@Immutable
data class AppElevation(
    val appBar: Dp = 5.dp,
    val elevation: Dp = 25.dp,
    /** 2 dp of elevation. */
    val dp2: Dp = 2.dp,
)

/**
 * Defines standardized size values for consistent component dimensions
 * across the application.
 *
 * This immutable data class provides size specifications that ensure
 * consistent dimensions for common UI elements like icons, avatars,
 * buttons, and input fields. These values are optimized for accessibility
 * and touch target requirements.
 *
 * Usage example:
 * ```
 * Icon(
 *     imageVector = Icons.Default.Home,
 *     contentDescription = "Home",
 *     modifier = Modifier.size(DesignTokens.sizes.iconMedium)
 * )
 *
 * Button(
 *     onClick = { },
 *     modifier = Modifier.height(DesignTokens.sizes.buttonHeight)
 * ) {
 *     Text("Action")
 * }
 *
 * AsyncImage(
 *     model = userAvatarUrl,
 *     contentDescription = "User Avatar",
 *     modifier = Modifier.size(DesignTokens.sizes.avatarLarge)
 * )
 * ```
 *
 * @param iconSmall Small icon size for compact layouts (16dp)
 * @param iconMedium Standard icon size for most use cases (24dp)
 * @param iconLarge Large icon size for prominent display (32dp)
 * @param avatarSmall Small avatar size for compact user representations
 *    (32dp)
 * @param avatarMedium Standard avatar size for most user displays (48dp)
 * @param avatarLarge Large avatar size for profile screens (64dp)
 * @param buttonHeight Standard height for button components (48dp)
 * @param inputHeight Standard height for input field components (56dp)
 * @param cardMinHeight Minimum height for card components to ensure proper
 *    proportions (120dp)
 * @param topAppBarHeight Height for top app bar components (40dp)
 * @param topBarStandardHeight Height for top bar components (40dp)
 */
@Immutable
data class AppSizes(
    /** Icon miny miny size: 8 dp. */
    val iconMinyMiny: Dp = 8.dp,
    /** Icon miny size: 12 dp. */
    val iconMiny: Dp = 12.dp,
    val iconSmall: Dp = 16.dp,
    /** Icon average size: 20 dp. */
    val iconAverage: Dp = 20.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    /** Icon extra large size: 36 dp. */
    val iconExtraLarge: Dp = 36.dp,
    val avatarMedium: Dp = 48.dp,
    /** Avatar medium extra size: 56 dp. */
    val avatarMediumExtra: Dp = 56.dp,
    val avatarLarge: Dp = 64.dp,
    /** Avatar large large size: 128 dp. */
    val avatarLargeLarge: Dp = 128.dp,
    val buttonHeight: Dp = 56.dp,
    /** Button height medium size: 40 dp. */
    val buttonHeightMedium: Dp = 40.dp,
    val inputHeight: Dp = 56.dp,
    /** Profile picture size: 72 dp. */
    val profile: Dp = 72.dp,
    val topBarStandardHeight: Dp = 40.dp,
    val topAppBarHeight: Dp = 85.dp,
    /** 14 dp image size. */
    val imageDp14: Dp = 14.dp,
    /** 40 dp image size. */
    val imageDp40: Dp = 40.dp,
    /** 48 dp image size. */
    val imageDp48: Dp = 48.dp,
    /** 100 dp image size. */
    val imageDp100: Dp = 100.dp,
    /** 128 dp image size. */
    val imageDp128: Dp = 128.dp,
    /** 256 dp image size. */
    val imageDp256: Dp = 256.dp,
    /** Height of a bounded lazy column: 500 dp. */
    val lazyColHeightInDp500: Dp = 500.dp,
    /** 80 dp of size. */
    val dp80: Dp = 80.dp,
    /** Table cell width small size: 65 dp. */
    val tableCellWidthSmall: Dp = 65.dp,
    /** Table cell width medium size: 100 dp. */
    val tableCellWidthMedium: Dp = 100.dp,
    /** Table cell width large size: 150 dp. */
    val tableCellWidthLarge: Dp = 150.dp,
)

/** Stroke-width tokens. */
@Immutable
data class AppStrokes(
    /** No stroke: 0 dp. */
    val none: Dp = 0.dp,
    /** 0.5 dp of stroke width. */
    val dpPoint5: Dp = 0.5.dp,
    /** Thin stroke width: 1 dp. */
    val thin: Dp = 1.dp,
    /** 2 dp of stroke width. */
    val dp2: Dp = 2.dp,
    /** 4 dp of stroke width. */
    val dp4: Dp = 4.dp,
)

/**
 * CompositionLocal provider for accessing [AppSpacing] values throughout
 * the composition tree.
 *
 * This provides a default instance of [AppSpacing] that can be overridden
 * by providing a different value through [DesignTokenTheme].
 */
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

/**
 * CompositionLocal provider for accessing [AppPadding] values throughout
 * the composition tree.
 *
 * This provides a default instance of [AppPadding] that can be overridden
 * by providing a different value through [DesignTokenTheme].
 */
val LocalPadding = staticCompositionLocalOf { AppPadding() }

/**
 * CompositionLocal provider for accessing [AppShapes] values throughout
 * the composition tree.
 *
 * This provides a default instance of [AppShapes] that can be overridden
 * by providing a different value through [DesignTokenTheme].
 */
val LocalShapes = staticCompositionLocalOf { AppShapes() }

/**
 * CompositionLocal provider for accessing [AppElevation] values throughout
 * the composition tree.
 *
 * This provides a default instance of [AppElevation] that can be
 * overridden by providing a different value through [DesignTokenTheme].
 */
val LocalAppElevation = staticCompositionLocalOf { AppElevation() }

/**
 * CompositionLocal provider for accessing [AppSizes] values throughout the
 * composition tree.
 *
 * This provides a default instance of [AppSizes] that can be overridden by
 * providing a different value through [DesignTokenTheme].
 */
val LocalSizes = staticCompositionLocalOf { AppSizes() }

/** Provides the current [AppStrokes]. */
val LocalStrokes = staticCompositionLocalOf { AppStrokes() }

/**
 * Theme provider composable that establishes the design token context for
 * the entire composition tree.
 *
 * This composable should be placed at the root of your application's
 * composition hierarchy to provide consistent access to design tokens
 * throughout all child composables. It uses CompositionLocal to
 * efficiently propagate design token values without explicit parameter
 * passing.
 *
 * Usage example:
 * ```
 * @Composable
 * fun MyApp() {
 *     DesignTokenTheme(
 *         spacing = AppSpacing(medium = 20.dp), // Custom spacing
 *         shapes = AppShapes(card = RoundedCornerShape(16.dp)) // Custom shapes
 *     ) {
 *         // Your app content here
 *         MainScreen()
 *     }
 * }
 * ```
 *
 * @param spacing Custom spacing configuration, defaults to [AppSpacing]
 *    with standard values
 * @param padding Custom padding configuration, defaults to [AppPadding]
 *    with standard values
 * @param shapes Custom shape configuration, defaults to [AppShapes] with
 *    standard values
 * @param elevation Custom elevation configuration, defaults to
 *    [AppElevation] with standard values
 * @param sizes Custom size configuration, defaults to [AppSizes] with
 *    standard values
 * @param content The composable content that will have access to these
 *    design tokens
 */
@Composable
internal fun DesignTokenTheme(
    spacing: AppSpacing = AppSpacing(),
    padding: AppPadding = AppPadding(),
    shapes: AppShapes = AppShapes(),
    elevation: AppElevation = AppElevation(),
    sizes: AppSizes = AppSizes(),
    strokes: AppStrokes = AppStrokes(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppSpacing provides spacing,
        LocalPadding provides padding,
        LocalShapes provides shapes,
        LocalAppElevation provides elevation,
        LocalSizes provides sizes,
        LocalStrokes provides strokes,
        content = content,
    )
}
