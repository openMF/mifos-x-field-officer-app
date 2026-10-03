/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientAddressEntity
import kpt.core.database.client.entity.ClientDateEntity
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.client.entity.ClientIdentifierEntity
import kpt.core.database.client.entity.ClientPayloadEntity
import kpt.core.database.client.entity.ClientsTemplateEntity
import kpt.core.database.client.entity.InterestTypeEntity
import kpt.core.database.client.entity.OfficeOptionsEntity
import kpt.core.database.client.entity.OptionsEntity
import kpt.core.database.client.entity.SavingProductOptionsEntity
import kpt.core.database.client.entity.StaffOptionsEntity
import kpt.core.database.datatable.entity.ColumnHeader
import kpt.core.database.datatable.entity.ColumnValue
import kpt.core.database.datatable.entity.DataTableEntity
import kpt.core.database.datatable.entity.DataTablePayload
import kpt.core.database.loan.entity.LoanAccountEntity
import kpt.core.database.savings.entity.SavingsAccountEntity

@DbDao
@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
    suspend fun insertClient(client: ClientEntity)

    @Query("SELECT * FROM Client")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM Client WHERE groupId = :groupId")
    fun getClientsByGroupId(groupId: Int): Flow<List<ClientEntity>>

    @Query("SELECT * FROM Client WHERE id = :clientId LIMIT 1")
    fun getClientByClientId(clientId: Int): Flow<ClientEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun insertLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingsAccountEntity::class)
    suspend fun insertSavingsAccount(savingsAccount: SavingsAccountEntity)

    @Query("SELECT * FROM LoanAccountEntity WHERE clientId = :clientId")
    fun getLoanAccountsByClientId(clientId: Long): Flow<List<LoanAccountEntity>>

    @Query("SELECT * FROM SavingsAccount WHERE clientId = :clientId")
    fun getSavingsAccountsByClientId(clientId: Long): Flow<List<SavingsAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientsTemplateEntity::class)
    suspend fun insertClientsTemplate(clientsTemplate: ClientsTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = OfficeOptionsEntity::class)
    suspend fun insertOfficeOptions(officeOptions: List<OfficeOptionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = StaffOptionsEntity::class)
    suspend fun insertStaffOptions(staffOptions: List<StaffOptionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingProductOptionsEntity::class)
    suspend fun insertSavingProductOptions(savingProductOptions: List<SavingProductOptionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = OptionsEntity::class)
    suspend fun insertOption(options: OptionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = InterestTypeEntity::class)
    suspend fun insertInterestTypes(interestTypes: List<InterestTypeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ColumnHeader::class)
    suspend fun insertColumnHeader(columnHeader: ColumnHeader)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ColumnValue::class)
    suspend fun insertColumnValue(columnValue: ColumnValue)

    @Query("DELETE FROM DataTable")
    suspend fun deleteDataTables()

    @Query("DELETE FROM ColumnHeader")
    suspend fun deleteColumnHeaders()

    @Query("DELETE FROM ColumnValue")
    suspend fun deleteColumnValues()

    @Query("SELECT * FROM ClientsTemplate LIMIT 1")
    suspend fun getClientsTemplate(): ClientsTemplateEntity

    @Query("SELECT * FROM ClientTemplateOfficeOptions")
    fun getOfficeOptions(): Flow<List<OfficeOptionsEntity>>

    @Query("SELECT * FROM ClientTemplateStaffOptions")
    fun getStaffOptions(): Flow<List<StaffOptionsEntity>>

    @Query("SELECT * FROM ClientTemplateSavingProductsOptions")
    fun getSavingProductOptions(): Flow<List<SavingProductOptionsEntity>>

    @Query("SELECT * FROM ClientTemplateOptions WHERE optionType = :optionType")
    fun getOptions(optionType: String): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM ClientTemplateInterest")
    fun getAllInterestType(): Flow<List<InterestTypeEntity>>

    @Query("SELECT * FROM DataTable where applicationTableName = :tableName")
    fun getDatatableByTableName(tableName: String): Flow<List<DataTableEntity>>

    @Query("SELECT * FROM ColumnHeader WHERE registeredTableName = :tableName")
    fun getColumnHeadersByTableName(tableName: String): Flow<List<ColumnHeader>>

    @Query("SELECT * FROM ColumnValue WHERE registeredTableName = :tableName")
    fun getColumnValuesByTableName(tableName: String): Flow<List<ColumnValue>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientPayloadEntity::class)
    suspend fun insertClientPayload(clientPayload: ClientPayloadEntity)

    @Insert(entity = DataTablePayload::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataTablePayload(dataTablePayloads: DataTablePayload)

    @Insert(entity = DataTableEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataTable(dataTable: DataTableEntity)

    @Query("SELECT * FROM ClientPayload")
    fun getAllClientPayload(): Flow<List<ClientPayloadEntity>>

    @Query("SELECT * FROM DataTablePayload WHERE clientCreationTime = :clientCreationTime")
    fun getDataTablePayloadByCreationTime(clientCreationTime: Long): Flow<List<DataTablePayload>>

    @Query("DELETE FROM ClientPayload WHERE id = :id")
    suspend fun deleteClientPayloadById(id: Int)

    @Query("DELETE FROM DataTablePayload WHERE clientCreationTime = :clientCreationTime")
    suspend fun deleteDataTablePayloadByCreationTime(clientCreationTime: Long)

    @Update(entity = ClientPayloadEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateDatabaseClientPayload(clientPayload: ClientPayloadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientAddressEntity::class)
    suspend fun insertAddress(address: ClientAddressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientAddressEntity::class)
    suspend fun insertAddresses(addresses: List<ClientAddressEntity>)

    @Query("SELECT * FROM ClientAddress WHERE clientID = :clientId")
    fun getAddressesByClientId(clientId: Int): Flow<List<ClientAddressEntity>>

    @Query("SELECT * FROM ClientAddress WHERE addressId = :addressId LIMIT 1")
    fun getAddressById(addressId: Int): Flow<ClientAddressEntity?>

    @Query("DELETE FROM ClientAddress WHERE clientID = :clientId")
    suspend fun deleteAddressesByClientId(clientId: Int)

    @Query(
        """
    SELECT * FROM ClientAddress 
    WHERE addressLine1 LIKE :query ESCAPE '\'
       OR addressLine2 LIKE :query ESCAPE '\'
       OR addressLine3 LIKE :query ESCAPE '\'
       OR city LIKE :query ESCAPE '\'
       OR stateName LIKE :query ESCAPE '\'
       OR countryName LIKE :query ESCAPE '\'
       OR postalCode LIKE :query ESCAPE '\'
    ORDER BY city
    LIMIT 50
    """,
    )
    fun searchAddressesByQuery(query: String): Flow<List<ClientAddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientIdentifierEntity::class)
    suspend fun insertIdentifiers(identifiers: List<ClientIdentifierEntity>)

    @Query("SELECT * FROM ClientIdentifier WHERE clientId = :clientId")
    fun getIdentifiersByClientId(clientId: Int): Flow<List<ClientIdentifierEntity>>

    @Query("DELETE FROM ClientIdentifier WHERE clientId = :clientId")
    suspend fun deleteIdentifiersByClientId(clientId: Int)

    @Query(
        """
    SELECT * FROM ClientIdentifier 
    WHERE CAST(id AS TEXT) LIKE :query ESCAPE '\'
       OR documentKey LIKE :query ESCAPE '\'
       OR description LIKE :query ESCAPE '\'
       OR documentTypeName LIKE :query ESCAPE '\'
    ORDER BY documentKey
    LIMIT 50
    """,
    )
    fun searchIdentifiersByQuery(query: String): Flow<List<ClientIdentifierEntity>>

    companion object {
        const val GENDER_OPTIONS = "genderOptions"
        const val CLIENT_TYPE_OPTIONS = "clientTypeOptions"
        const val CLIENT_CLASSIFICATION_OPTIONS = "clientClassificationOptions"
    }

    /**
     * Replace every identifier belonging to the touched clients.
     *
     * Was `ClientDaoHelper.insertIdentifiers`, which hand-rolled a backup/restore around the
     * delete+insert to fake atomicity — reading every row back, then re-inserting it from memory if
     * the write threw. `@Transaction` is that guarantee for free, and a real rollback rather than a
     * best-effort one (the hand-rolled restore could itself throw, which it only logged).
     */
    @Transaction
    suspend fun replaceIdentifiersForClients(identifiers: List<ClientIdentifierEntity>) {
        val valid = identifiers.filter { it.clientId != null }
        if (valid.isEmpty()) return
        valid.mapNotNull { it.clientId }.distinct().forEach { deleteIdentifiersByClientId(it) }
        insertIdentifiers(valid)
    }

    /** Persist a client, deriving its split date columns. Was `ClientDaoHelper.saveClient`. */
    suspend fun saveClientWithDate(client: ClientEntity) {
        val date = if (client.activationDate.size >= 3) {
            ClientDateEntity(
                clientId = client.id,
                chargeId = 0,
                day = client.activationDate[0] ?: 0,
                month = client.activationDate[1] ?: 0,
                year = client.activationDate[2] ?: 0,
            )
        } else {
            null
        }
        insertClient(if (date != null) client.copy(clientDate = date) else client)
    }

    /**
     * Read a client back with `activationDate` rebuilt — the inverse of [saveClientWithDate].
     * Was `ClientDaoHelper.getClient`.
     */
    fun observeClientWithDate(clientId: Int): Flow<ClientEntity?> =
        getClientByClientId(clientId).map { client ->
            client?.copy(
                activationDate = listOf(
                    client.clientDate?.day,
                    client.clientDate?.month,
                    client.clientDate?.year,
                ),
            )
        }

    /** Was `ClientDaoHelper.saveClientAccounts` — now atomic across both tables. */
    @Transaction
    suspend fun saveClientAccounts(clientAccounts: ClientAccounts, clientId: Int) {
        val owner = clientId.toLong()
        clientAccounts.loanAccounts.forEach { insertLoanAccount(it.copy(clientId = owner)) }
        clientAccounts.savingsAccounts.forEach { insertSavingsAccount(it.copy(clientId = owner)) }
    }

    /**
     * Fan one client template out across the option, datatable and header/value tables.
     *
     * Was `ClientDaoHelper.saveClientTemplate`. Two things changed:
     *
     * 1. It is a transaction. The original wrote 8+ tables unguarded, so a mid-way failure left the
     *    template rows disagreeing with the option rows.
     * 2. The datatable tables are cleared ONCE, before the loop. The original called
     *    `deleteDataTables()` / `deleteColumnHeaders()` / `deleteColumnValues()` INSIDE
     *    `for (dataTable in ...)`, so every iteration wiped what the previous one wrote and only
     *    the last datatable survived.
     *
     * The `optionType` stamp is load-bearing: gender, client-type and classification options all
     * share one `OptionsEntity` table and are told apart only by that discriminator.
     */
    @Transaction
    suspend fun saveClientTemplate(clientsTemplate: ClientsTemplateEntity) {
        insertClientsTemplate(clientsTemplate)
        clientsTemplate.officeOptions?.let { insertOfficeOptions(it) }
        clientsTemplate.staffOptions?.let { insertStaffOptions(it) }
        clientsTemplate.savingProductOptions?.let { insertSavingProductOptions(it) }
        clientsTemplate.genderOptions?.forEach { insertOption(it.copy(optionType = GENDER_OPTIONS)) }
        clientsTemplate.clientTypeOptions?.forEach { insertOption(it.copy(optionType = CLIENT_TYPE_OPTIONS)) }
        clientsTemplate.clientClassificationOptions?.forEach {
            insertOption(it.copy(optionType = CLIENT_CLASSIFICATION_OPTIONS))
        }
        clientsTemplate.clientLegalFormOptions?.let { insertInterestTypes(it) }

        clientsTemplate.dataTables?.let { dataTables ->
            deleteDataTables()
            deleteColumnHeaders()
            deleteColumnValues()
            dataTables.forEach { dataTable ->
                insertDataTable(dataTable)
                dataTable.columnHeaderData.forEach { header ->
                    insertColumnHeader(header.copy(registeredTableName = dataTable.applicationTableName))
                    header.columnValues.forEach { value ->
                        insertColumnValue(value.copy(registeredTableName = dataTable.registeredTableName))
                    }
                }
            }
        }
    }

    /**
     * Drop an offline client payload together with the datatable rows queued alongside it.
     * Was `ClientDaoHelper.deleteAndUpdatePayloads` — now atomic, so a half-deleted payload cannot
     * be replayed on the next sync.
     */
    @Transaction
    suspend fun deleteClientPayloadWithData(id: Int, clientCreationTime: Long) {
        deleteClientPayloadById(id)
        deleteDataTablePayloadByCreationTime(clientCreationTime)
    }

    /** One queued offline client, for the write store's source of truth. */
    @Query("SELECT * FROM ClientPayload WHERE id = :id LIMIT 1")
    fun observeClientPayload(id: Int): Flow<ClientPayloadEntity?>
}
