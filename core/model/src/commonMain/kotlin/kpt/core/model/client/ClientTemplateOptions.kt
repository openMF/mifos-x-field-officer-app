/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
import kpt.core.model.objects.account.share.ShareAccounts

/**
 * The option sets Fineract offers for ONE client — the domain projection of
 * `GET {resourceType}/{clientId}/template`.
 *
 * ## Why this type exists rather than passing the response envelope through
 *
 * `GetClientsPageItemsResponse` is the wire envelope: it carries the client's own identity fields
 * (`accountNo`, `displayName`, `officeName`, `legalForm`, `dateOfBirth`, …) *alongside* the option
 * sets, because Fineract returns one document for both. Only the option sets are what the template
 * is READ for — the client's identity comes from `clientStream`, which is cached separately and is
 * the one the detail screen already renders.
 *
 * Narrowing it here is what keeps `core:network` out of the feature modules. A repository that
 * returned the envelope would force every screen rendering a staff dropdown to depend on the
 * network module — the layering the Clients slice already rejected once — and `ScreenDataStream`
 * has an `internal` constructor, so a feature cannot map the type back down on its own. The
 * projection has to happen at or below the store, and the store is where the fetch already is.
 *
 * ## Three screens, one read
 *
 * The original had three repository methods — `getClientStaffOptions`, `getSavingsAccounts`,
 * `getShareAccounts` — that each called `getClientTemplate(clientId)` and kept a single field, so
 * opening the three screens cost three identical uncached requests. They share this one store.
 */
@Serializable
data class ClientTemplateOptions(
    /** Loan officers this client may be assigned to. Backs the assign-staff dropdown. */
    val staffOptions: List<StaffOption> = emptyList(),
    /** The client's savings accounts, as default-account candidates. */
    val savingAccountOptions: List<SavingAccountOption> = emptyList(),
    /** The client's share accounts. */
    val shareAccounts: List<ShareAccounts> = emptyList(),
)

/**
 * A savings account offered as a choice on a client form.
 *
 * Declared here rather than reused from `core/network/.../savings/dto` for the same reason as
 * [StaffOption]: it is read by a screen, so it belongs on the domain side of the seam. The network
 * DTO of the same name stays where it is — it is part of the savings API surface, which no feature
 * reads directly.
 */
@Serializable
data class SavingAccountOption(
    val id: Long = 0,
    val accountNo: String = "",
)
