/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import com.niyajali.compose.sign.ComposeSign
import com.niyajali.compose.sign.SignatureConfig
import com.niyajali.compose.sign.exportSignature
import com.niyajali.compose.sign.isNotEmpty
import com.niyajali.compose.sign.rememberSignatureState
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppCustomDialog
import kpt.core.designsystem.fieldofficer.component.AppTopBar
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken

/** Dialog for drawing a signature; saving returns it as an image. */
@Composable
fun AppSignatureDrawDialog(
    onSaveFile: (ImageBitmap) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val signatureState = rememberSignatureState()
    var size = remember {
        Size.Zero
    }

    AppCustomDialog(
        onDismiss = { onDismiss() },
        modifier = modifier,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            modifier = Modifier.padding(DesignToken.padding.large),
            shape = DesignToken.shapes.small,
        ) {
            Column {
                AppTopBar(
                    modifier = Modifier.height(DesignToken.sizes.topBarStandardHeight),
                    topBarTitle = "",
                    backPress = {
                        onDismiss()
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                signatureState.clear()
                            },
                        ) {
                            Icon(AppIcons.Refresh, null)
                        }

                        IconButton(
                            onClick = {
                                signatureState.undo()
                            },
                        ) {
                            Icon(AppIcons.Undo, null)
                        }
                        IconButton(
                            onClick = {
                                signatureState.redo()
                            },
                        ) {
                            Icon(AppIcons.Redo, null)
                        }
                        IconButton(
                            onClick = {
                                val data = signatureState.exportSignature(
                                    width = size.width.toInt(),
                                    height = size.height.toInt(),
                                    backgroundColor = Color.White,
                                )

                                data?.let {
                                    onSaveFile(it)
                                }
                            },
                            enabled = signatureState.isNotEmpty(),
                        ) {
                            Icon(AppIcons.Check, null)
                        }
                    },
                )

                HorizontalDivider(
                    thickness = DesignToken.strokes.thin,
                    color = KptTheme.colorScheme.outlineVariant,
                )

                ComposeSign(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned {
                            size = it.size.toSize()
                        },
                    config = SignatureConfig(
                        backgroundColor = Color.Transparent,
                        borderStroke = null,
                        showGrid = true,
                    ),
                    state = signatureState,
                    // intentional-noop: the pad is read through `signatureState` (clear / undo / redo /
                    // export below), so the per-stroke callback has nothing to do.
                    onSignatureUpdate = {},
                )
            }
        }
    }
}
