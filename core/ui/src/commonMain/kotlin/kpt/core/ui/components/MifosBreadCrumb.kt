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

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.designsystem.theme.MifosTypography
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.bread_crumb_back_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosBreadcrumbNavBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val backStack = navController.currentBackStack.value
        .mapNotNull { it.destination.route }
        .filter { it.isNotBlank() }
        .filter { it.contains("Route", ignoreCase = true) }

    fun formatRoute(route: String): String {
        var simpleName = route.substringBefore('?').substringAfterLast('.')

        if (simpleName.contains("Route", ignoreCase = true)) {
            simpleName = simpleName.substringBefore("Route") + "Route"
        }

        if (simpleName.contains("Screen", ignoreCase = true)) {
            simpleName = simpleName.substringBefore("Screen") + "Screen"
        }

        simpleName = simpleName.removeSuffix("Route").removeSuffix("Screen")

        return simpleName.replace(Regex("([a-z])([A-Z])"), "$1 $2")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
            .padding(DesignToken.padding.large),
    ) {
        val routes = if (backStack.size <= 3) {
            backStack
        } else {
            backStack.take(2) + listOf("...") + backStack.takeLast(1)
        }

        Row(Modifier.weight(1f)) {
            routes.forEachIndexed { index, route ->
                when (route) {
                    "..." -> Text(" ... > ", style = MifosTypography.bodySmallEmphasized)
                    else -> {
                        BreadcrumbItem(
                            text = formatRoute(route),
                            isActive = index == routes.lastIndex,
                            onClick = {
                                if (index != routes.lastIndex && route != "...") {
                                    navController.popBackStack(route, inclusive = false)
                                }
                            },
                        )
                        if (index != routes.lastIndex) {
                            Text(" > ", style = MifosTypography.bodySmallEmphasized)
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(DesignToken.sizes.iconMedium),
        ) {
            Icon(
                painter = painterResource(Res.drawable.bread_crumb_back_icon),
                contentDescription = "Back",
                modifier = Modifier.size(DesignToken.sizes.iconSmall),
            )
        }
    }
}

@Composable
fun BreadcrumbItem(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        color = if (isActive) KptTheme.colorScheme.primary else KptTheme.colorScheme.onBackground,
        modifier = Modifier.clickable(
            enabled = !isActive,
            interactionSource = MutableInteractionSource(),
            indication = LocalIndication.current,
        ) { onClick() },
        style = MifosTypography.bodySmallEmphasized,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
    )
}

@Preview
@Composable
private fun BreadcrumbItemPreview() {
    MifosTheme {
        Row(
            modifier = Modifier.padding(DesignToken.padding.medium),
        ) {
            BreadcrumbItem(
                text = "Home",
                isActive = false,
                onClick = {},
            )
            Text(" > ")
            BreadcrumbItem(
                text = "Loan Account",
                isActive = true,
                onClick = {},
            )
        }
    }
}
