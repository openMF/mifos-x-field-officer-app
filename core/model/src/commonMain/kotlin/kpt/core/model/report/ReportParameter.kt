/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.report

import kotlinx.serialization.Serializable

/**
 * One input a report asks for, from Fineract's `FullParameterList` (`stretchy_parameter`).
 *
 * [name] is the parameter's own report (`runreports/{name}` lists its options); [variable] is what the
 * report SQL substitutes — sent as `R_{variable}`. [parentName] names the parameter whose value narrows
 * this one's options (a loan officer list depends on the office).
 */
@Serializable
data class ReportParameter(
    val name: String,
    val variable: String,
    /** The label shown for the parameter. */
    val label: String,
    /** How the parameter is entered. */
    val displayType: ReportParameterType,
    /** The parameter's default value. */
    val defaultValue: String? = null,
    /** Fineract's `selectAll`: the options include "All", sent as -1. */
    val allowAll: Boolean = false,
    val parentName: String? = null,
) {
    /** The query key the report run takes this parameter's value under. */
    val queryKey: String get() = "R_$variable"
}

/**
 * Kind of a report parameter.
 *
 * @property DATE A date
 * @property HIDDEN A hidden parameter, filled in by the app
 * @property SELECT A pick-one-of parameter
 * @property TEXT free text
 */
@Serializable
enum class ReportParameterType {
    /** Chosen from a list of options. */
    SELECT,

    /** A date. */
    DATE,

    /** Free text. */
    TEXT,

    /** Filled in by the app, never shown. */
    HIDDEN,
}

/**
 * A choosable value of a SELECT parameter: what is sent, and what the officer sees.
 *
 * @property id the server id of this record
 * @property label the label shown for the parameter
 */
@Serializable
data class ReportOption(val id: String, val label: String)
