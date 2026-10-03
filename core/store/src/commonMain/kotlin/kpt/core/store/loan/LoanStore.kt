/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.loan

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.loan.dao.LoanDao
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.network.mifos.loan.api.LoanApi
import kpt.core.network.mifos.loan.dto.LoanWithAssociationsDto
import kpt.core.network.mifos.loan.mapper.LoanAccountMapper
import kpt.core.network.mifos.loan.mapper.toDomain
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The one place the loan's three representations meet: the wire DTO (175 fields), the Room entity
 * (56 — the subset worth persisting) and the domain model the UI reads. The fetcher lands the DTO,
 * the writer narrows it to the entity, and the reader widens the entity back to the domain, so no
 * caller ever sees a `*Dto` or a `*Entity`.
 */
@StoreProvider(id = "loans", ttl = "15m")
@CacheKey(fn = "byId", key = "loan:{loanId}", params = ["loanId:Int"])
fun provideLoanStore(
    service: LoanApi,
    dao: LoanDao,
): Store<Int, LoanWithAssociations> = StoreFactory.createStore(
    fetcher = Fetcher.of { loanId: Int -> service.getLoanByIdWithAllAssociations(loanId) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { loanId: Int ->
            dao.getLoanById(loanId).map { row -> row?.let(LoanAccountMapper::mapFromEntity) }
        },
        writer = { _: Int, dto: LoanWithAssociationsDto ->
            dao.saveLoanWithAssociations(LoanAccountMapper.mapToEntity(dto.toDomain()))
        },
    ),
)
