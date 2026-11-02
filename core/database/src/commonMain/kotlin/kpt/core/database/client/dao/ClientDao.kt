/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientDateEntity
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.client.entity.ClientsTemplateEntity
import kpt.core.database.loan.entity.LoanAccountEntity
import kpt.core.database.savings.entity.SavingsAccountEntity

@DbDao
@Dao
interface ClientDao :
    ClientTemplateOptionsDao,
    ClientDataTableDao,
    ClientPayloadDao,
    ClientAddressIdentifierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientEntity::class)
    suspend fun insertClient(client: ClientEntity)

    @Query("SELECT * FROM Client")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM Client WHERE groupId = :groupId")
    fun getClientsByGroupId(groupId: Int): Flow<List<ClientEntity>>

    @Query("SELECT * FROM Client WHERE id = :clientId LIMIT 1")
    fun getClientByClientId(clientId: Int): Flow<ClientEntity?>

    /** Per-key clear for the `clientRow` write store. */
    @Query("DELETE FROM Client WHERE id = :clientId")
    suspend fun deleteClientById(clientId: Int)

    /** The roster store's `deleteAll` — what the logout purge reaches the `Client` table through. */
    @Query("DELETE FROM Client")
    suspend fun deleteAllClients()

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun insertLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingsAccountEntity::class)
    suspend fun insertSavingsAccount(savingsAccount: SavingsAccountEntity)

    @Query("SELECT * FROM LoanAccountEntity WHERE clientId = :clientId")
    fun getLoanAccountsByClientId(clientId: Long): Flow<List<LoanAccountEntity>>

    @Query("SELECT * FROM SavingsAccount WHERE clientId = :clientId")
    fun getSavingsAccountsByClientId(clientId: Long): Flow<List<SavingsAccountEntity>>

    companion object {
        const val GENDER_OPTIONS = "genderOptions"
        const val CLIENT_TYPE_OPTIONS = "clientTypeOptions"
        const val CLIENT_CLASSIFICATION_OPTIONS = "clientClassificationOptions"
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
        clientsTemplate.dataTables?.let { replaceDataTables(it) }
    }
}
