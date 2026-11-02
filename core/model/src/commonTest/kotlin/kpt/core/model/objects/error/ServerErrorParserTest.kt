/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.error

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerErrorParserTest {

    // Verbatim shape of a Ktor ClientRequestException message for a Fineract 400 (device, 2026-10-24).
    private val ktor400 =
        """Client request(POST https://sandbox.mifos.community/fineract-provider/api/v1/clients) """ +
            """invalid: 400 Bad Request. Text: "{"developerMessage":"The request was invalid.",""" +
            """"httpStatusCode":"400","defaultUserMessage":"Validation errors exist.",""" +
            """"userMessageGlobalisationCode":"validation.msg.validation.errors.exist",""" +
            """"errors":[{"developerMessage":"Activation date cannot be in the future.",""" +
            """"defaultUserMessage":"Activation date cannot be in the future.",""" +
            """"parameterName":"activationDate","args":[{"value":"2026-10-24"}]}]}""""

    @Test
    fun aFieldErrorIsPreferredOverTheGenericTopLevelMessage() {
        assertEquals("Activation date cannot be in the future.", RuntimeException(ktor400).serverErrorMessage())
    }

    @Test
    fun theParsedErrorKeepsItsStatusAndFields() {
        val error = ServerErrorParser.parse(RuntimeException(ktor400))
        assertEquals("400", error?.httpStatusCode)
        assertEquals("activationDate", error?.errors?.single()?.parameterName)
    }

    @Test
    fun withoutFieldErrorsTheTopLevelMessageIsUsed() {
        val body =
            """{"httpStatusCode":"403",""" +
                """"defaultUserMessage":"This charge cannot be updated as the loan is not pending approval",""" +
                """"errors":[]}"""
        assertEquals(
            "This charge cannot be updated as the loan is not pending approval",
            ServerErrorParser.parse(body)?.userMessage,
        )
    }

    @Test
    fun severalFieldErrorsAreListedOncePerLine() {
        val body =
            """{"errors":[{"defaultUserMessage":"A is required."},""" +
                """{"defaultUserMessage":"B is too long."},{"defaultUserMessage":"A is required."}]}"""
        assertEquals("A is required.\nB is too long.", ServerErrorParser.parse(body)?.userMessage)
    }

    @Test
    fun aWrappedServerErrorIsStillFound() {
        val wrapped = IllegalStateException("create identifier failed", RuntimeException(ktor400))
        assertEquals("Activation date cannot be in the future.", wrapped.serverErrorMessage())
    }

    @Test
    fun aPermissionRefusalSaysWhatWasRefused() {
        // Verbatim gateway 403 for the fieldofficer role (apis.mifos.community, 2026-10-24).
        val ktor403 =
            """Client request(POST https://apis.mifos.community/1.0/field/v1/collectionsheet""" +
                """?command=generateCollectionSheet) invalid: 403 Forbidden. Text: "{"developerMessage":""" +
                """"The user associated with credentials passed on this request does not have """ +
                """sufficient privileges to perform this action.",""" +
                """"httpStatusCode":"403",""" +
                """"defaultUserMessage":"Insufficient privileges to perform this action.",""" +
                """"userMessageGlobalisationCode":"error.msg.not.authorized",""" +
                """"errors":[{"developerMessage":"User has no authority to READ collectionsheets",""" +
                """"defaultUserMessage":"User has no authority to READ collectionsheets",""" +
                """"userMessageGlobalisationCode":"error.msg.not.authorized","parameterName":"id",""" +
                """"args":[]}]}""""
        assertEquals("User has no authority to READ collectionsheets", RuntimeException(ktor403).serverErrorMessage())
    }

    @Test
    fun anythingThatIsNotAFineractBodyIsNull() {
        assertNull(RuntimeException("Unable to resolve host \"sandbox.mifos.community\"").serverErrorMessage())
        // A 500 carrying Spring's body, not Fineract's — no user message in it.
        assertNull(
            ServerErrorParser.parse(
                """Server error(POST …) invalid: 500. Text: "{"timestamp":"2026-10-24T04:33:21Z",""" +
                    """"status":500,"error":"Internal Server Error"}"""",
            ),
        )
        assertNull(RuntimeException("Text: \"{not json").serverErrorMessage())
        assertNull((null as Throwable?).serverErrorMessage())
    }
}
