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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Titled card listing an address, one labelled line per field. */
@Composable
fun AppAddressCard(
    title: String,
    addressList: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline {
        Column(
            modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraSmall),
        ) {
            Text(
                text = title,
                style = AppTypography.titleMediumEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.medium))
            addressList.forEach { (key, value) ->
                AppAddressItem(key, value)
                Spacer(Modifier.height(DesignToken.padding.small))
            }
        }
    }
}

/** One labelled line of an address card. */
@Composable
fun AppAddressItem(
    addressLabel: String,
    addressValue: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
    ) {
        Text(
            text = addressLabel,
            style = AppTypography.labelSmall,
            color = KptTheme.colorScheme.secondary,
        )
        Text(
            text = addressValue,
            style = AppTypography.bodySmall,
        )
    }
}

@Preview
@Composable
private fun AppAddressCardPreview() {
    val sampleAddresses = mapOf(
        "Address 1" to "123, MG Road",
        "Address 2" to "2nd Floor, Lotus Building",
        "City" to "Bengaluru",
        "State" to "Karnataka",
        "Pincode" to "560001",
        "Country" to "India",
    )

    AppTheme {
        AppAddressCard(
            title = "Client Address",
            addressList = sampleAddresses,
        )
    }
}
