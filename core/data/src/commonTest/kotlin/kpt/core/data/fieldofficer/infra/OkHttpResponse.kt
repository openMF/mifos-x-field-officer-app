/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.fieldofficer.infra

import io.ktor.client.call.HttpClientCall
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlin.coroutines.EmptyCoroutineContext

/** A bare 200 for the fake Apis whose endpoint returns a raw [HttpResponse] nobody reads. */
@OptIn(InternalAPI::class)
object OkHttpResponse : HttpResponse() {
    override val call: HttpClientCall get() = error("no call behind a fake response")
    override val status = HttpStatusCode.OK
    override val version = HttpProtocolVersion.HTTP_1_1
    override val requestTime = GMTDate()
    override val responseTime = GMTDate()
    override val rawContent: ByteReadChannel get() = ByteReadChannel.Empty
    override val headers: Headers = Headers.Empty
    override val coroutineContext = EmptyCoroutineContext
}
