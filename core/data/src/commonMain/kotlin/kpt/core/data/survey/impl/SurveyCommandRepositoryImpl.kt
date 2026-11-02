/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.survey.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.survey.SurveyCommandRepository
import kpt.core.model.objects.surveys.Scorecard
import kpt.core.network.mifos.survey.api.SurveyApi

@RepositoryBinding(binds = SurveyCommandRepository::class)
internal class SurveyCommandRepositoryImpl(
    private val surveyApi: SurveyApi,
    private val gateway: MutationGateway,
) : SurveyCommandRepository {

    /**
     * The one write in this codebase with NO local side, and deliberately so: nothing reads a
     * scorecard back. There is no scorecard table, no scorecard read endpoint in `SurveyApi`, and
     * the `surveys` store holds definitions, which a submission does not change. Recording it would
     * need a new table (a schema change) for a read no screen makes; add `localApply` with that read.
     */
    override suspend fun submitScore(surveyId: Int, scorecardPayload: Scorecard?): MutationResult<Scorecard> =
        gateway.command(
            CommandSpec(
                payload = surveyId,
                endpoint = { surveyApi.submitScore(surveyId = it, scorecardPayload = scorecardPayload) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )
}
