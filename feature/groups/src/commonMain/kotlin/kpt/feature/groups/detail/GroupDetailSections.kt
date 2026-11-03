/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.icon.ForkIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.model.client.Client
import kpt.core.model.loan.LoanAccount
import kpt.core.model.savings.SavingsAccount

/**
 * The original's expandable account cards (`MifosLoanAccountExpendableCard` /
 * `MifosSavingsAccountExpendableCard`, `6b66e8a43:feature/groups/.../GroupDetailsScreen.kt:407`), plus
 * the group's clients — the original's "Group clients" overflow entry, here as the same kind of card
 * because the members are already in hand. Collapsed by default, as the original was.
 *
 * Status dot: the original's palette, as the app's status tokens (`AppColors.loan*Status`) — active /
 * approved-awaiting-disbursal / pending approval / in arrears / anything else.
 */
@Composable
internal fun ExpandableSection(
    title: String,
    count: Int,
    tag: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "expand")
    OutlinedCard(modifier = modifier.fillMaxWidth().animateContentSize().testTag(tag)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "$title ($count)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(AppIcons.KeyboardArrowDown, contentDescription = null, modifier = Modifier.rotate(rotation))
        }
        if (expanded) {
            HorizontalDivider()
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
internal fun LoanAccountRow(account: LoanAccount, onClick: ((Int) -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    val status = account.status
    AccountRow(
        title = account.productName.orEmpty(),
        subtitle = account.accountNo.orEmpty(),
        dot = when {
            status?.active == true && account.inArrears == true -> colors.error
            status?.active == true -> AppColors.loanActiveStatus
            status?.waitingForDisbursal == true -> colors.primary
            status?.pendingApproval == true -> AppColors.loanPendingStatus
            else -> colors.outline
        },
        onClick = onClick?.let { open -> account.id?.let { id -> { open(id) } } },
    )
}

@Composable
internal fun SavingsAccountRow(account: SavingsAccount, onClick: ((Int, String) -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    val status = account.status
    AccountRow(
        title = account.productName.orEmpty(),
        subtitle = account.accountNo.orEmpty(),
        dot = when {
            status?.active == true -> AppColors.loanActiveStatus
            status?.approved == true -> colors.primary
            status?.submittedAndPendingApproval == true -> AppColors.loanPendingStatus
            else -> colors.outline
        },
        // The deposit type picks the endpoint (savings vs recurring) — the original passed it too.
        onClick = onClick?.let { open ->
            account.id?.let { id -> account.depositType?.let { type -> { open(id, type.endpoint) } } }
        },
    )
}

@Composable
internal fun ClientRow(client: Client, onClick: ((Int) -> Unit)?) {
    AccountRow(
        title = client.displayName.orEmpty(),
        subtitle = client.accountNo.orEmpty(),
        dot = if (client.active) AppColors.loanActiveStatus else MaterialTheme.colorScheme.outline,
        onClick = onClick?.let { open -> { open(client.id) } },
    )
}

/** A row is tappable only when it has somewhere to go — never drawn clickable and inert. */
@Composable
private fun AccountRow(title: String, subtitle: String, dot: Color, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(modifier = Modifier.size(10.dp)) { drawCircle(dot) }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (onClick != null) {
            Icon(
                ForkIcons.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
