/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_fab_create
import kpt.core.ui.generated.resources.core_ui_fab_create_center
import kpt.core.ui.generated.resources.core_ui_fab_create_client
import kpt.core.ui.generated.resources.core_ui_fab_create_group
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** What a create button creates. */
enum class FabType {
    /** A client. */
    CLIENT,

    /** A centre. */
    CENTER,

    /** A group. */
    GROUP,
}

/** Whether the create buttons are shown. */
sealed class FabButtonState {
    /** Hidden. */
    data object Collapsed : FabButtonState()

    /** Shown. */
    data object Expand : FabButtonState()

    /** True when shown. */
    fun isExpanded() = this == Expand

    /** The other state. */
    fun toggleValue() = if (isExpanded()) {
        Collapsed
    } else {
        Expand
    }
}

/**
 * One create button.
 *
 * @property fabType what it creates
 */
sealed class FabButton(val fabType: FabType) {
    /**
     * Create button with a drawable icon.
     *
     * @property type what it creates
     * @property iconRes its icon
     */
    data class DrawableFab(val type: FabType, val iconRes: DrawableResource) : FabButton(type)

    /**
     * Create button with a vector icon.
     *
     * @property type what it creates
     * @property iconRes its icon
     */
    data class VectorFab(val type: FabType, val iconRes: ImageVector) : FabButton(type)
}

/** One create button. */
@Composable
fun FabItem(
    fabButton: FabButton,
    modifier: Modifier = Modifier,
    onFabClick: (FabType) -> Unit,
) {
    FloatingActionButton(
        onClick = { onFabClick(fabButton.fabType) },
        modifier = modifier.size(DesignToken.sizes.imageDp48),
    ) {
        when (fabButton) {
            // A spoken label, not the enum's name: TalkBack read "CLIENT" / "CENTER" in every locale.
            is FabButton.DrawableFab -> Icon(
                painter = painterResource(fabButton.iconRes),
                contentDescription = stringResource(fabButton.fabType.labelRes()),
            )
            is FabButton.VectorFab -> Icon(
                imageVector = fabButton.iconRes,
                contentDescription = stringResource(fabButton.fabType.labelRes()),
            )
        }
    }
}

/** Expanding floating action button that reveals the create buttons. */
@Composable
fun MultiFloatingActionButton(
    fabButtons: List<FabButton>,
    fabButtonState: FabButtonState,
    onFabButtonStateChange: (FabButtonState) -> Unit,
    modifier: Modifier = Modifier,
    onFabClick: (FabType) -> Unit,
) {
    val rotation by animateFloatAsState(
        if (fabButtonState.isExpanded()) {
            45f
        } else {
            0f
        },
        label = "mainFabRotation",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = fabButtonState.isExpanded(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.large)) {
                fabButtons.forEach {
                    FabItem(
                        fabButton = it,
                        onFabClick = onFabClick,
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = {
                onFabButtonStateChange(fabButtonState.toggleValue())
            },
            modifier = Modifier
                .rotate(rotation),
        ) {
            Icon(
                imageVector = AppIcons.Add,
                contentDescription = stringResource(Res.string.core_ui_fab_create),
            )
        }
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarPreview() {
    AppTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarWithActionsPreview() {
    AppTheme {
        SelectionModeTopAppBar(
            itemCount = 5,
            resetSelectionMode = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = AppIcons.Delete,
                        contentDescription = "delete",
                    )
                }
            },
        )
    }
}

private fun FabType.labelRes(): StringResource = when (this) {
    FabType.CLIENT -> Res.string.core_ui_fab_create_client
    FabType.CENTER -> Res.string.core_ui_fab_create_center
    FabType.GROUP -> Res.string.core_ui_fab_create_group
}
