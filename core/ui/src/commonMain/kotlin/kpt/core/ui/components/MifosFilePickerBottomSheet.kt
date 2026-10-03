/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.component.MifosBottomSheet
import kpt.core.designsystem.component.MifosBottomSheetOptionItem
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.file_picker_bottom_sheet_files
import kpt.core.ui.generated.resources.file_picker_bottom_sheet_gallery
import kpt.core.ui.generated.resources.file_picker_bottom_sheet_more
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosFilePickerBottomSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onFilesClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMoreClick: () -> Unit,
) {
    MifosBottomSheet(
        onDismiss = onDismiss,
        modifier = modifier,
        containerColor = KptTheme.colorScheme.onPrimary,
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = DesignToken.padding.large,
                    end = DesignToken.padding.large,
                    bottom = DesignToken.padding.extraLarge,
                ),
        ) {
            MifosBottomSheetOptionItem(
                label = stringResource(Res.string.file_picker_bottom_sheet_gallery),
                icon = MifosIcons.Gallery,
                onClick = onGalleryClick,
            )
            MifosBottomSheetOptionItem(
                label = stringResource(Res.string.file_picker_bottom_sheet_files),
                icon = MifosIcons.PickDocument,
                onClick = onFilesClick,
            )
            MifosBottomSheetOptionItem(
                label = stringResource(Res.string.file_picker_bottom_sheet_more),
                icon = MifosIcons.MoreHoriz,
                onClick = onMoreClick,
            )
        }
    }
}

@Preview
@Composable
fun PreviewMifosFilePickerBottomSheet() {
    KptTheme {
        MifosFilePickerBottomSheet(
            {},
            {},
            {},
        ) {}
    }
}
