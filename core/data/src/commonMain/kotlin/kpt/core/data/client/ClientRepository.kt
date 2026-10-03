/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientEntity

interface ClientRepository {
    /** The officer's client roster. */
    fun clientsStream(scope: CoroutineScope): ScreenDataStream<List<ClientEntity>>

    /** One client, by id — backs the detail screen. */
    fun clientStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientEntity>

    /**
     * A client's loan and savings accounts.
     *
     * The original fetched these inside the same use-case as the client itself and wrote both into
     * separate StateFlows; they are separate streams here so the detail screen can render identity
     * while accounts are still loading, instead of blocking the whole screen on the slower of two.
     */
    fun clientAccountsStream(clientId: Int, scope: CoroutineScope): ScreenDataStream<ClientAccounts>
}
