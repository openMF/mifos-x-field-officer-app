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

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download

internal actual suspend fun saveReportCsv(fileName: String, csv: ByteArray): Boolean {
    FileKit.download(bytes = csv, fileName = "$fileName.csv")
    return true
}
