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

enum class FabType {
    CLIENT,
    CENTER,
    GROUP,
}

sealed class FabButtonState {
    data object Collapsed : FabButtonState()

    data object Expand : FabButtonState()

    fun isExpanded() = this == Expand

    fun toggleValue() = if (isExpanded()) {
        Collapsed
    } else {
        Expand
    }
}

sealed class FabButton(val fabType: FabType) {
    data class DrawableFab(val type: FabType, val iconRes: DrawableResource) : FabButton(type)
    data class VectorFab(val type: FabType, val iconRes: ImageVector) : FabButton(type)
}

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
