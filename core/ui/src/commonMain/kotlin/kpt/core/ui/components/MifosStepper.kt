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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

data class Step(
    val name: String,
    val content: @Composable () -> Unit,
)

@Composable
fun MifosStepper(
    steps: List<Step>,
    currentIndex: Int,
    onStepChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(currentIndex) {
        listState.animateScrollToItem(currentIndex)
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = DesignToken.padding.small,
                horizontal = DesignToken.padding.large,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .clip(shape = DesignToken.shapes.medium)
                .background(KptTheme.colorScheme.primary)
                .padding(
                    vertical = DesignToken.padding.largeIncreasedExtra,
                )
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = DesignToken.padding.small),
            ) {
                steps.forEachIndexed { index, step ->
                    repeat(2) { it ->
                        item {
                            if (it == 0) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(DesignToken.sizes.avatarMediumExtra),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(DesignToken.sizes.iconLarge)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    index == currentIndex -> KptTheme.colorScheme.onPrimary
                                                    else -> KptTheme.colorScheme.primaryContainer
                                                },
                                            )
                                            .clickable(enabled = index < currentIndex) {
                                                if (index < currentIndex) onStepChange(index)
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = (index + 1).toString(),
                                            color = when {
                                                index == currentIndex -> KptTheme.colorScheme.primary
                                                else -> KptTheme.colorScheme.onPrimaryContainer
                                            },
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(DesignToken.padding.small))
                                    BasicText(
                                        text = step.name,
                                        autoSize = TextAutoSize.StepBased(
                                            minFontSize = 2.sp,
                                            maxFontSize = 11.sp,
                                        ),
                                        style = MifosTypography.labelSmall.copy(
                                            color = KptTheme.colorScheme.onPrimary,
                                        ),
                                    )
                                }
                            } else if (index != steps.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = DesignToken.padding.large)
                                        .width(DesignToken.padding.small)
                                        .height(DesignToken.padding.dp1)
                                        .background(KptTheme.colorScheme.primaryContainer),
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(DesignToken.padding.largeIncreased))
        steps[currentIndex].content()
    }
}

@Preview
@Composable
private fun MifosStepperDemo() {
    val steps = listOf(
        Step("Details") { Text("Step 1: Details Content") },
        Step("Terms") { Text("Step 2: Terms Content") },
        Step("Charges") { Text("Step 3: Charges Content") },
        Step("Schedule") { Text("Step 4: Schedule Content") },
        Step("Preview") { Text("Step 5: Preview Content") },
    )

    MifosStepper(
        steps = steps,
        currentIndex = 0,
        onStepChange = { },
        modifier = Modifier
            .fillMaxWidth(),
    )
}
