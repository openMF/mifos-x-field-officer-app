/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerConfig(
    val protocol: String,
    @SerialName("end_point")
    val endPoint: String,
    @SerialName("api_path")
    val apiPath: String,
    val port: String,
    val tenant: String,
) {
    companion object {
        val DEFAULT = ServerConfig(
            protocol = "https://",
            endPoint = "apis.mifos.community",
            apiPath = "/1.0/field/v1/",
            port = "443",
            tenant = "mifos-bank-1",
        )

        val LOCALHOST = ServerConfig(
            protocol = "http://",
            endPoint = "localhost",
            apiPath = "/fineract-provider/api/v1/",
            port = "8080",
            tenant = "default",
        )
    }
}

fun ServerConfig.getInstanceUrl(): String {
    return "$protocol$endPoint$apiPath"
}
