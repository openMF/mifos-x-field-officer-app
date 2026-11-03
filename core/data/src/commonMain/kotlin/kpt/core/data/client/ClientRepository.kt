/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.client.AddressConfiguration
import kpt.core.model.client.AddressTemplate
import kpt.core.model.client.Client
import kpt.core.model.client.ClientAccounts
import kpt.core.model.client.ClientCloseTemplateResponse
import kpt.core.model.client.ClientTemplateOptions
import kpt.core.model.client.ClientsTemplate
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.noncoreobjects.IdentifierTemplate
import kpt.core.model.shared.CollateralItem
import kpt.core.model.shared.CollateralItemResult

/** Reads clients and everything shown on them, from the cache and the server. */
interface ClientRepository {
    /** The officer's client roster. */
    fun clientsStream(scope: CoroutineScope): ScreenDataStream<List<Client>>

    /** One client, by id — backs the detail screen. */
    fun clientStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<Client>

    /**
     * A client's loan and savings accounts.
     *
     * The original fetched these inside the same use-case as the client itself and wrote both into
     * separate StateFlows; they are separate streams here so the detail screen can render identity
     * while accounts are still loading, instead of blocking the whole screen on the slower of two.
     */
    fun clientAccountsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientAccounts>

    /**
     * The client's identity documents (passport, national id, …).
     *
     * The STORE for this read already existed (`clientGetClientListIdentifiers`) and nothing
     * exposed it — the identifiers screen could not be ported without this line. That is the usual
     * shape of the remaining feature work: the substrate is wired API-to-store, and the repository
     * surface is completed per feature as its screens land.
     */
    fun clientIdentifiersStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<List<Identifier>>

    /**
     * The document TYPES this client may be identified by.
     *
     * The store behind it (`@StoreProvider(id = "clientGetClientIdentifierTemplate")`) already
     * existed and was unexposed — the add-identifier form is its first reader.
     */
    fun clientIdentifierTemplateStream(clientId: Long, scope: CoroutineScope): ScreenDataStream<IdentifierTemplate>

    /**
     * The client's profile photo as the `data:image/...;base64,...` URL Fineract returns, or
     * `null` when there isn't one.
     *
     * A one-shot blob, NOT a [ScreenDataStream]: a missing photo is the common case rather than an
     * error, so there is no Loading/Empty/Error for a screen to render — the avatar placeholder IS
     * the empty state. Failure returns `null` for the same reason; an unreachable photo must not
     * turn the client's profile into an error screen.
     *
     * Returned as the raw STRING, decoded by the caller through `core/ui`'s `imageToByteArray`.
     * That is where the original put the conversion too (`Flow<DataState<String>>`), and it keeps
     * `core/data` from depending on `core/ui` — the dependency runs the other way.
     */
    suspend fun clientImage(clientId: Int): String?

    /** The client's postal addresses. */
    fun clientAddressesStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<ClientAddressEntity>>

    /**
     * The address FORM's option sets (address types, states, countries). A tenant-level catalog,
     * so keyed `Unit` and shared across clients — the original fetched it per client.
     */
    /**
     * The TENANT's client template — gender / client-type / classification / legal-form code
     * values, and the tenant's configured data tables.
     *
     * NOT the same read as [clientTemplateStream], which is per-client. The create-client and
     * edit-details forms both need this one; it is keyed by nothing because it is identical for
     * every client in the tenant.
     *
     * Named for the API that supplies it (`clients/template` -> `ClientsTemplate`). The
     * create form and the edit form were ported independently and asked for this under two
     * different names; there is one read, so there is one method.
     */
    fun clientsTemplateStream(scope: CoroutineScope): ScreenDataStream<ClientsTemplate>

    /**
     * Whether this tenant collects client ADDRESSES at all, and how.
     *
     * The store behind this (`@StoreProvider(id = "addressConfiguration")`) already existed and was
     * unexposed — the create-client form is its first reader. A tenant with addresses disabled must
     * not be shown the address block, which is also why the original's unconditional address
     * validation made client creation impossible on such a tenant.
     */
    fun addressConfigurationStream(scope: CoroutineScope): ScreenDataStream<AddressConfiguration>

    /** The address template: address types and the fields each one takes. */
    fun addressTemplateStream(scope: CoroutineScope): ScreenDataStream<AddressTemplate>

    /** Where the officer has recorded this client — the pinpoint map/list. */
    fun pinpointLocationsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<ClientAddressResponse>>

    /** Collateral the client has already pledged. */
    fun clientCollateralStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<List<CollateralItemResult>>

    /**
     * The collateral TYPES the tenant offers — a catalog, not client data, which is why it is keyed
     * `Unit` and shared across clients. Backs the type picker on the add-collateral form.
     */
    fun collateralItemsStream(scope: CoroutineScope): ScreenDataStream<List<CollateralItem>>

    /** Closure reasons for the close-client form. Also a tenant-level catalog. */
    fun clientCloseTemplateStream(scope: CoroutineScope): ScreenDataStream<ClientCloseTemplateResponse>

    /**
     * The client's TEMPLATE — the option sets the Fineract server offers for THIS client:
     * `staffOptions`, `savingAccountOptions`, `shareAccounts`.
     *
     * One read backing several screens, which is how the original got it too: `getClientStaff`,
     * `getSavingsAccounts` and `getShareAccounts` were three repository methods that each called
     * `getClientTemplate(clientId)` and kept one field. Three identical uncached requests became
     * one store; the screens project the field they need.
     */
    fun clientTemplateStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientTemplateOptions>
}
