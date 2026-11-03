/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.searchrecord

/**
 * One record search: which kind of record, and the text to match. In core/model because the screen
 * builds it — a feature sees core/data + core/model only.
 *
 * @property query the text searched for
 * @property type the record's type
 */
data class SearchRecordKey(val type: RecordType, val query: String)
