/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.mapper

/** Maps between a stored entity and its domain model. */
interface EntityMapper<Entity, DomainModel> {
    /** Maps a stored entity to its domain model. */
    fun mapFromEntity(entity: Entity): DomainModel

    /** Maps a domain model to its stored entity. */
    fun mapToEntity(domainModel: DomainModel): Entity

    /** Maps stored entities to domain models. */
    fun mapFromEntityList(entities: List<Entity>): List<DomainModel>

    /** Maps domain models to stored entities. */
    fun mapToEntityList(domainModels: List<DomainModel>): List<Entity>
}
