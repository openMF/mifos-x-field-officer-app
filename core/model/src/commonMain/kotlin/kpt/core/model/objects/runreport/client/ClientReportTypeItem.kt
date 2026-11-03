/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.runreport.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 03-08-17.
 */
@Serializable
data class ClientReportTypeItem(
    /** The server id of the parameter. */
    @SerialName("parameter_id")
    var parameterId: Int? = null,

    /** The name of the parameter. */
    @SerialName("parameter_name")
    var parameterName: String? = null,

    /** The report's category. */
    @SerialName("report_category")
    var reportCategory: String? = null,

    /** The server id of the report. */
    @SerialName("report_id")
    var reportId: Int? = null,

    /** The report's name. */
    @SerialName("report_name")
    var reportName: String? = null,

    /** The name the report expects the parameter under. */
    @SerialName("report_parameter_name")
    var reportParameterName: String? = null,

    /** The report's subtype. */
    @SerialName("report_subtype")
    var reportSubtype: String? = null,

    /** The report's type (table, chart, …). */
    @SerialName("report_type")
    var reportType: String? = null,
)
