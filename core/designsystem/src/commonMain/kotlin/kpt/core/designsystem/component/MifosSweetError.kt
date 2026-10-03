/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.generated.resources.Res
import kpt.core.designsystem.generated.resources.core_designsystem_try_again
import kpt.core.designsystem.generated.resources.core_designsystem_unable_to_load
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosSweetError(
    message: String,
    isShowLoadMsg: Boolean = true,
    modifier: Modifier = Modifier
        .fillMaxSize()
        .padding(DesignToken.padding.dp18)
        .semantics { contentDescription = "MifosSweetError" },
    isRetryEnabled: Boolean = true,
    buttonText: String = stringResource(Res.string.core_designsystem_try_again),
    onclick: () -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isShowLoadMsg) {
            Text(
                text = stringResource(Res.string.core_designsystem_unable_to_load),
                style = KptTheme.typography.bodyMedium,
                color = KptTheme.colorScheme.secondary,
            )
        }
        Text(
            text = message,
            style = KptTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            color = KptTheme.colorScheme.secondary,
        )
        if (isRetryEnabled) {
            Spacer(modifier = Modifier.height(DesignToken.spacing.largeIncreased))
            Button(
                onClick = { onclick() },
                contentPadding = PaddingValues(),
            ) {
                Text(
                    modifier = Modifier.padding(start = DesignToken.spacing.largeIncreased, end = DesignToken.spacing.largeIncreased),
                    text = buttonText,
                    style = KptTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun MifosPaginationSweetError(
    modifier: Modifier = Modifier,
    onclick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignToken.padding.dp18),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info Image",
        )
        Text(
            text = stringResource(Res.string.core_designsystem_unable_to_load),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.secondary,
        )
        Button(
            onClick = { onclick() },
            contentPadding = PaddingValues(),
        ) {
            Text(
                modifier = Modifier
                    .padding(start = DesignToken.padding.largeIncreased, end = DesignToken.padding.largeIncreased),
                text = stringResource(Res.string.core_designsystem_try_again),
                style = KptTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview
@Composable
private fun MifosSweetErrorPreview() {
    MifosTheme {
        MifosSweetError(
            message = "Something went wrong. Please check your network.",
            buttonText = "Retry",
            onclick = {},
        )
    }
}

@Preview
@Composable
private fun MifosSweetError_NoRetryPreview() {
    MifosTheme {
        MifosSweetError(
            message = "Failed to fetch data.",
            isRetryEnabled = false,
        )
    }
}

@Preview
@Composable
private fun MifosPaginationSweetErrorPreview() {
    MifosTheme {
        MifosPaginationSweetError(
            onclick = {},
        )
    }
}
