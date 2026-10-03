/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
            is FabButton.DrawableFab -> Icon(
                painter = painterResource(fabButton.iconRes),
                contentDescription = fabButton.fabType.name,
            )
            is FabButton.VectorFab -> Icon(
                imageVector = fabButton.iconRes,
                contentDescription = fabButton.fabType.name,
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
                imageVector = MifosIcons.Add,
                contentDescription = "mainFabIcon",
            )
        }
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarPreview() {
    MifosTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarWithActionsPreview() {
    MifosTheme {
        SelectionModeTopAppBar(
            itemCount = 5,
            resetSelectionMode = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = MifosIcons.Delete,
                        contentDescription = "delete",
                    )
                }
            },
        )
    }
}
