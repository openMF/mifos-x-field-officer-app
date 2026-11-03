/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.client.ClientRepository
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

/**
 * Test double for [ClientRepository], owned by the module that owns the CONTRACT — same reasoning
 * as [FakeClientCommandRepository] (see its KDoc for why `core/data` and why `commonMain`).
 *
 * ## Why this exists rather than an anonymous `object : ClientRepository` per test
 *
 * It was one per test, and each one listed every method. `ClientRepository` grew four streams in
 * S3c and every one of those anonymous fakes stopped compiling — in tests that do not read any of
 * the new streams. That is a maintenance cost with no diagnostic value: a test for the identifiers
 * screen has nothing to say about the collateral catalog.
 *
 * ## Unimplemented streams THROW, and that is the point
 *
 * Each stream errors unless the test supplied a state flow for it. A fake that handed back an
 * empty stream instead would let a ViewModel read the WRONG stream and still pass — which is how
 * the stale-fake defect earlier in this port went unnoticed until a test was read closely.
 *
 * Every supplied stream records the key it was asked for, so a test can prove the route argument
 * arrived rather than the `SavedStateHandle` default.
 */
@ExperimentalDataTestingApi
class FakeClientRepository(
    private val clients: MutableStateFlow<ScreenState<List<Client>>>? = null,
    private val client: MutableStateFlow<ScreenState<Client>>? = null,
    private val accounts: MutableStateFlow<ScreenState<ClientAccounts>>? = null,
    private val identifiers: MutableStateFlow<ScreenState<List<Identifier>>>? = null,
    private val pinpoints: MutableStateFlow<ScreenState<List<ClientAddressResponse>>>? = null,
    private val clientCollateral: MutableStateFlow<ScreenState<List<CollateralItemResult>>>? = null,
    private val collateralCatalog: MutableStateFlow<ScreenState<List<CollateralItem>>>? = null,
    private val clientsTemplate: MutableStateFlow<ScreenState<ClientsTemplate>>? = null,
    private val identifierTemplate: MutableStateFlow<ScreenState<IdentifierTemplate>>? = null,
    private val addressConfiguration: MutableStateFlow<ScreenState<AddressConfiguration>>? = null,
    private val closeTemplate: MutableStateFlow<ScreenState<ClientCloseTemplateResponse>>? = null,
    private val template: MutableStateFlow<ScreenState<ClientTemplateOptions>>? = null,
    private val addresses: MutableStateFlow<ScreenState<List<ClientAddressEntity>>>? = null,
    private val addressTemplate: MutableStateFlow<ScreenState<AddressTemplate>>? = null,
    /** The `data:` URL [ClientRepository.clientImage] returns; `null` models "no photo". */
    private val image: String? = null,
) : ClientRepository {

    /**
     * One refresh trigger PER STREAM, not one shared.
     *
     * A shared trigger would let `retryReachesTheStreamThatFailed` pass when a retry hit the wrong
     * stream — which is the single thing that test exists to catch. Read one with
     * [refreshesFor]; [refreshes] is the convenience for a fake with only one stream.
     */
    private val triggers: MutableMap<String, MutableSharedFlow<Unit>> = mutableMapOf()

    /** The refresh signal of one stream, so a test can see it re-read. */
    fun refreshesFor(stream: String): MutableSharedFlow<Unit> =
        triggers.getOrPut(stream) { MutableSharedFlow(extraBufferCapacity = 8) }

    /**
     * The trigger of the ONLY stream this fake was given. Throws when it was given several — a
     * test with two streams must name which one it means, or it is asserting nothing.
     */
    val refreshes: MutableSharedFlow<Unit>
        get() = triggers.values.singleOrNull()
            ?: error("several streams are wired; use refreshesFor(<stream>) instead (${triggers.keys})")

    /** Every key each stream was asked for, keyed by stream name. */
    val requestedKeys: MutableMap<String, MutableList<Any>> = mutableMapOf()

    private fun <T> stream(name: String, key: Any?, flow: MutableStateFlow<ScreenState<T>>?): ScreenDataStream<T> {
        val source = requireNotNull(flow) {
            "FakeClientRepository: $name was read but no state flow was supplied for it"
        }
        if (key != null) requestedKeys.getOrPut(name) { mutableListOf() } += key
        return screenDataStreamForTesting(source, refreshTrigger = refreshesFor(name))
    }

    override fun clientsStream(scope: CoroutineScope) = stream("clients", null, clients)

    override fun clientStream(clientId: Int, scope: CoroutineScope) =
        stream("client", clientId, client)

    override fun clientAccountsStream(clientId: Int, scope: CoroutineScope) =
        stream("accounts", clientId, accounts)

    override fun clientIdentifiersStream(clientId: Long, scope: CoroutineScope) =
        stream("identifiers", clientId, identifiers)

    override fun pinpointLocationsStream(clientId: Int, scope: CoroutineScope) =
        stream("pinpoints", clientId, pinpoints)

    override fun clientCollateralStream(clientId: Int, scope: CoroutineScope) =
        stream("clientCollateral", clientId, clientCollateral)

    override fun collateralItemsStream(scope: CoroutineScope) =
        stream("collateralCatalog", null, collateralCatalog)

    override fun clientsTemplateStream(scope: CoroutineScope) =
        stream("clientsTemplate", null, clientsTemplate)

    override fun clientIdentifierTemplateStream(clientId: Long, scope: CoroutineScope) =
        stream("identifierTemplate", clientId, identifierTemplate)

    override fun addressConfigurationStream(scope: CoroutineScope) =
        stream("addressConfiguration", null, addressConfiguration)

    override fun clientCloseTemplateStream(scope: CoroutineScope) =
        stream("closeTemplate", null, closeTemplate)

    override fun clientTemplateStream(clientId: Int, scope: CoroutineScope) =
        stream("template", clientId, template)

    /** Client ids the photo was requested for. */
    val imageRequests: MutableList<Int> = mutableListOf()

    override fun clientAddressesStream(clientId: Int, scope: CoroutineScope) =
        stream("addresses", clientId, addresses)

    override fun addressTemplateStream(scope: CoroutineScope) =
        stream("addressTemplate", null, addressTemplate)

    override suspend fun clientImage(clientId: Int): String? {
        imageRequests += clientId
        return image
    }
}
