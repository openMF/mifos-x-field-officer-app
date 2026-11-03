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
 *
 * @property key the form's draft key
 */
enum class CreateForm(val key: String) {
    /** The new-client form. */
    CLIENT("create.client"),

    /** The new-group form. */
    GROUP("create.group"),

    /** The new-centre form. */
    CENTER("create.center"),

    /** The new-loan application. */
    LOAN("create.loan"),

    /** The group-loan application. */
    GROUP_LOAN("create.group_loan"),

    /** The savings application. */
    SAVINGS("create.savings"),

    /** The add-address form. */
    ADDRESS("create.address"),

    /** The fixed-deposit application. */
    FIXED_DEPOSIT("create.fixed_deposit"),

    /** The recurring-deposit application. */
    RECURRING_DEPOSIT("create.recurring_deposit"),

    /** The share-account application. */
    SHARE("create.share"),

    /** The pledge-collateral form. */
    COLLATERAL("create.collateral"),

    /** The default-savings-account form. */
    DEFAULT_ACCOUNT("create.default_account"),
    ;

    /** Constants and factories of `CreateForm`. */
    companion object {
        /** The prefix every create-form draft key starts with. */
        const val PREFIX = "create."

        /** The form with this persisted key, or null for an unknown key. */
        fun fromKey(key: String): CreateForm? = entries.firstOrNull { it.key == key }
    }
}

/** The address create's two path arguments travel with its body — the payload alone cannot be replayed. */
@Serializable
data class AddressDraftPayload(
    /** The server id of the client. */
    val clientId: Int,
    /** The server id of the address type. */
    val addressTypeId: Int,
    /** The address request saved in the draft. */
    val request: kpt.core.model.client.PostClientAddressRequest,
)
