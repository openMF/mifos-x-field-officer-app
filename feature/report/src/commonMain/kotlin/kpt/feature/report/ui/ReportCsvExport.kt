/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.ui

/**
 * Saves [csv] as `[fileName].csv` where the platform keeps a user's files; true when it was saved.
 *
 * Android / desktop / iOS: the platform's own save dialog (no storage permission) — false when the
 * user cancels. Web: a browser download — there is no dialog to cancel.
 */
internal expect suspend fun saveReportCsv(fileName: String, csv: ByteArray): Boolean
