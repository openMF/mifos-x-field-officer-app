/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

/**
 * A failed write as it really arrives: Ktor's exception, whose `message` is the request line
 * followed by Fineract's JSON body carrying [userMessage] as the field error.
 *
 * Use this — not `IllegalStateException("the reason")` — to fake a server refusal. A bare exception
 * whose message IS the reason made the raw-message bug untestable: the ViewModel printed
 * `cause.message` and the test passed, while on device the same line printed the URL and the JSON.
 */
@ExperimentalDataTestingApi
fun fineractRejection(userMessage: String, status: Int = 403): Throwable = RuntimeException(
    "Client request(POST https://fineract.test/fineract-provider/api/v1/command) invalid: " +
        "$status. Text: \"" +
        """{"developerMessage":"Errors contain reason for domain rule violation.","httpStatusCode":"$status",""" +
        """"defaultUserMessage":"Errors contain reason for domain rule violation.",""" +
        """"errors":[{"developerMessage":"$userMessage","defaultUserMessage":"$userMessage"}]}""" +
        "\"",
)
