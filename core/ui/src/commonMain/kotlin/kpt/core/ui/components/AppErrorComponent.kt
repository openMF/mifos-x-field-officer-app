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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_no_data
import kpt.core.ui.generated.resources.core_ui_no_internet
import kpt.core.ui.generated.resources.core_ui_retry
import kpt.core.ui.generated.resources.core_ui_something_went_wrong
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Error state: no-internet, empty-data or a message, with an optional retry. */
@Composable
fun AppErrorComponent(
    modifier: Modifier = Modifier,
    isNetworkConnected: Boolean = true,
    message: String? = null,
    isEmptyData: Boolean = false,
    isRetryEnabled: Boolean = false,
    onRetry: () -> Unit = {},
) {
    when {
        !isNetworkConnected -> NoInternetComponent(isRetryEnabled = isRetryEnabled) { onRetry() }
        else -> EmptyDataComponent(
            modifier = modifier,
            isEmptyData = isEmptyData,
            message = message,
            isRetryEnabled = isRetryEnabled,
            onRetry = onRetry,
        )
    }
}

/** No-internet state with an optional retry. */
@Composable
fun NoInternetComponent(
    modifier: Modifier = Modifier,
    isRetryEnabled: Boolean = false,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .size(DesignToken.sizes.imageDp100)
                .padding(bottom = DesignToken.padding.medium),
            imageVector = AppIcons.WifiOff,
            contentDescription = "Wifi Icon",
        )

        Text(
            text = stringResource(Res.string.core_ui_no_internet),
            style = TextStyle(fontSize = 20.sp),
        )

        Spacer(modifier = Modifier.height(DesignToken.spacing.medium))

        if (isRetryEnabled) {
            FilledTonalButton(onClick = { onRetry.invoke() }) {
                Text(text = stringResource(Res.string.core_ui_retry))
            }
        }
    }
}

/** Empty-data or error state with an optional retry. */
@Composable
fun EmptyDataComponent(
    modifier: Modifier = Modifier,
    isEmptyData: Boolean = false,
    message: String? = null,
    isRetryEnabled: Boolean = false,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .size(DesignToken.sizes.imageDp100)
                .padding(bottom = DesignToken.padding.medium),
            imageVector = AppIcons.Info,
            contentDescription = "Info Icon",
        )

        Text(
            modifier = Modifier.padding(horizontal = DesignToken.padding.largeIncreased),
            text = message ?: if (isEmptyData) {
                stringResource(Res.string.core_ui_no_data)
            } else {
                stringResource(Res.string.core_ui_something_went_wrong)
            },
            style = TextStyle(fontSize = 20.sp),
            textAlign = TextAlign.Center,
        )

        if (isRetryEnabled) {
            FilledTonalButton(
                modifier = Modifier.padding(top = DesignToken.padding.small),
                onClick = { onRetry.invoke() },
            ) {
                Text(text = stringResource(Res.string.core_ui_retry))
            }
        }
    }
}

/** Empty-data state with a custom message and icon. */
@Composable
fun EmptyDataComponentWithModifiedMessageAndIcon(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isEmptyData: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            modifier = Modifier
                .size(DesignToken.sizes.imageDp100)
                .padding(bottom = DesignToken.padding.medium),
            imageVector = if (isEmptyData) icon else AppIcons.Info,
            contentDescription = "Info Icon",
        )

        Text(
            modifier = Modifier.padding(horizontal = DesignToken.padding.largeIncreased),
            text = if (isEmptyData) message else stringResource(Res.string.core_ui_something_went_wrong),
            style = TextStyle(fontSize = 20.sp),
            textAlign = TextAlign.Center,
            color = KptTheme.colorScheme.error,

        )
    }
}

@Preview
@Composable
private fun NoInternetPreview() {
    AppTheme {
        NoInternetComponent()
    }
}

@Preview
@Composable
private fun EmptyDataPreview() {
    AppTheme {
        EmptyDataComponent()
    }
}

@Preview
@Composable
private fun EmptyDataComponentWithModifiedMessageAndIconPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        EmptyDataComponentWithModifiedMessageAndIcon(
            message = "No data found",
            icon = AppIcons.Error,
            modifier = modifier,
            isEmptyData = true,
        )
    }
}
