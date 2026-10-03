/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalFoundationApi::class)

package kpt.core.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.designsystem.utility.TabContent
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosTabRow(
    tabContents: List<TabContent>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    containerColor: Color = KptTheme.colorScheme.surface,
    selectedContentColor: Color = KptTheme.colorScheme.primary,
    unselectedContentColor: Color = KptTheme.colorScheme.onSurfaceVariant,
) {
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        TabRow(
            containerColor = containerColor,
            selectedTabIndex = pagerState.currentPage,
            indicator = {},
            divider = {},
        ) {
            tabContents.forEachIndexed { index, currentTab ->
                Tab(
                    text = { Text(text = currentTab.tabName) },
                    selected = pagerState.currentPage == index,
                    selectedContentColor = selectedContentColor,
                    unselectedContentColor = unselectedContentColor,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            tabContents.getOrNull(page)?.content?.invoke() ?: Text("Page $page")
        }
    }
}

@Preview
@Composable
private fun MifosTabRowPreview() {
    val pagerState = rememberPagerState { 2 }
    val tabContents = listOf(
        TabContent("Home") { Text("Home Content") },
        TabContent("Profile") { Text("Profile Content") },
    )

    MifosTheme {
        MifosTabRow(
            tabContents = tabContents,
            pagerState = pagerState,
        )
    }
}
