/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.ui

import androidx.compose.runtime.Composable
import kpt.core.common.utils.Constants
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.RecordType
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * The original's four preview states (`SearchRecordPreviewProvider`), as INTERNAL previews: the
 * device-free render tier (`CommonComposablePreviewScanner`) cannot see private ones. Values are
 * fixture data, not app copy — `// i18n:skip`.
 */

// i18n:skip — preview fixtures
private val previewRecords = listOf(
    GenericSearchRecord(
        id = 1,
        name = "Home Address",
        description = "Delhi, India",
        type = RecordType.ADDRESS.name,
        metadata = mapOf(
            Constants.ADDRESS_LINE_1 to "123 Main",
            Constants.CITY to "Delhi",
            Constants.COUNTRY to "India",
        ),
    ),
    GenericSearchRecord(
        id = 2,
        name = "Passport",
        description = "Valid Document",
        type = RecordType.IDENTIFIER.name,
        metadata = mapOf(Constants.DOCUMENT_KEY to "A12345678", Constants.STATUS to "Active"),
    ),
)

@Preview
@Composable
internal fun SearchRecordScreenResultsPreview() {
    KptTheme {
        // i18n:skip — preview fixture
        SearchRecordScreen(
            state = SearchRecordState(
                recordType = RecordType.ADDRESS,
                searchQuery = "Kartikey",
                searchRecords = previewRecords,
            ),
        )
    }
}

@Preview
@Composable
internal fun SearchRecordScreenLoadingPreview() {
    KptTheme {
        SearchRecordScreen(
            state = SearchRecordState(
                recordType = RecordType.IDENTIFIER,
                dialogState = SearchRecordState.DialogState.Loading,
            ),
        )
    }
}

@Preview
@Composable
internal fun SearchRecordScreenNoResultsPreview() {
    KptTheme {
        // i18n:skip — preview fixture
        SearchRecordScreen(
            state = SearchRecordState(
                recordType = RecordType.ADDRESS,
                searchQuery = "Unknown",
                isNoResultsFound = true,
            ),
        )
    }
}

@Preview
@Composable
internal fun SearchRecordScreenErrorPreview() {
    KptTheme {
        // i18n:skip — preview fixture
        SearchRecordScreen(
            state = SearchRecordState(
                recordType = RecordType.ADDRESS,
                dialogState = SearchRecordState.DialogState.Error("Network Timeout"),
            ),
        )
    }
}
