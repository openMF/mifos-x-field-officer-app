/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppUserImage
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_email_na
import kpt.feature.client.generated.resources.feature_client_mobile_na
import kpt.feature.client.generated.resources.feature_client_name_na
import org.jetbrains.compose.resources.stringResource

/**
 * Ported verbatim from
 * `6b66e8a43:feature/client/clientDetailsProfile/components/ClientDetailsProfile.kt` — the avatar
 * plus name / mobile / email, with the original's three N/A fallbacks.
 *
 * Unlike the profile screen's `ProfileCard` this is NOT a card and NOT tappable: it is the header
 * of the screen the card leads to.
 */
@Composable
internal fun ClientDetailsHeader(
    image: ByteArray?,
    name: String?,
    mobile: String?,
    email: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = KptTheme.spacing.lg, bottom = DesignToken.padding.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppUserImage(
            bitmap = image,
            modifier = Modifier.size(DesignToken.sizes.avatarLarge),
        )
        Spacer(Modifier.width(DesignToken.padding.medium))
        Column(
            Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
        ) {
            Text(
                text = name ?: stringResource(Res.string.feature_client_name_na),
                style = AppTypography.titleMediumEmphasized,
            )
            Text(
                text = mobile ?: stringResource(Res.string.feature_client_mobile_na),
                style = AppTypography.bodySmall,
                color = KptTheme.colorScheme.secondary,
            )
            Text(
                text = email ?: stringResource(Res.string.feature_client_email_na),
                style = AppTypography.bodySmall,
                color = KptTheme.colorScheme.secondary,
            )
        }
    }
}
