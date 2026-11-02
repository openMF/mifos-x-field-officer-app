/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.draft

import kotlinx.serialization.Serializable

/**
 * Every create flow that keeps a draft (core-base `FormDraft`). [key] is its `form` / outbox
 * `formKey`; all share [PREFIX] so the create outbox can claim exactly these rows.
 */
enum class CreateForm(val key: String) {
    CLIENT("create.client"),
    GROUP("create.group"),
    CENTER("create.center"),
    LOAN("create.loan"),
    GROUP_LOAN("create.group_loan"),
    SAVINGS("create.savings"),
    ADDRESS("create.address"),
    FIXED_DEPOSIT("create.fixed_deposit"),
    RECURRING_DEPOSIT("create.recurring_deposit"),
    SHARE("create.share"),
    COLLATERAL("create.collateral"),
    DEFAULT_ACCOUNT("create.default_account"),
    ;

    companion object {
        const val PREFIX = "create."

        fun fromKey(key: String): CreateForm? = entries.firstOrNull { it.key == key }
    }
}

/** The address create's two path arguments travel with its body — the payload alone cannot be replayed. */
@Serializable
data class AddressDraftPayload(
    val clientId: Int,
    val addressTypeId: Int,
    val request: kpt.core.model.client.PostClientAddressRequest,
)
