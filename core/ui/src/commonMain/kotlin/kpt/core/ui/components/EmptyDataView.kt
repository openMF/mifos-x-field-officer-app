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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_no_internet
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Centred icon and error message. */
@Composable
fun EmptyDataView(
    error: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    icon: ImageVector = AppIcons.Error,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .size(DesignToken.sizes.imageDp100)
                .padding(bottom = DesignToken.padding.medium),
            imageVector = icon,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.padding(horizontal = DesignToken.padding.largeIncreased),
            text = error,
            style = KptTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
        )
    }
}

/** Centred image and error message, from a string resource. */
@Composable
fun EmptyDataView(
    error: StringResource,
    modifier: Modifier = Modifier.fillMaxSize(),
    image: DrawableResource? = null,
    errorString: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        image?.let {
            Icon(
                modifier = Modifier
                    .size(DesignToken.sizes.imageDp100)
                    .padding(bottom = DesignToken.padding.medium),
                painter = painterResource(it),
                contentDescription = null,
            )
        }

        Text(
            modifier = Modifier.padding(horizontal = DesignToken.padding.largeIncreased),
            text = errorString ?: stringResource(error),
            style = KptTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun EmptyDataViewPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        EmptyDataView(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            image = null,
        )
    }
}

@Preview
@Composable
private fun EmptyDataViewPreviewForIcon(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        EmptyDataView(
            error = "This is error",
            modifier = modifier,
        )
    }
}
