package com.example.data

import com.example.crypto.Base32
import com.example.crypto.HashAlgorithm
import com.example.crypto.KeystoreEncryption
import com.example.crypto.OtpType
import com.example.model.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepository(
    private val accountDao: AccountDao,
    private val folderDao: FolderDao,
    private val passwordDao: PasswordDao? = null
) {

    val allPasswords: Flow<List<PasswordEntity>> = passwordDao?.getAllPasswords() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertPassword(password: PasswordEntity): Long {
        return passwordDao?.insertPassword(password) ?: 0L
    }

    suspend fun updatePassword(password: PasswordEntity) {
        passwordDao?.updatePassword(password)
    }

    suspend fun deletePasswordById(id: Long) {
        passwordDao?.deletePasswordById(id)
    }

    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts().map { entities ->
        entities.map { entityToAccount(it) }
    }

    val allFolders: Flow<List<FolderEntity>> = folderDao.getAllFolders()

    fun getAccountsByFolder(folderId: Long): Flow<List<Account>> {
        return accountDao.getAccountsByFolder(folderId).map { entities ->
            entities.map { entityToAccount(it) }
        }
    }

    suspend fun getAccountById(id: Long): Account? {
        val entity = accountDao.getAccountById(id) ?: return null
        return entityToAccount(entity)
    }

    suspend fun insertAccount(account: Account): Long {
        val entity = accountToEntity(account)
        return accountDao.insertAccount(entity)
    }

    suspend fun insertAccounts(accounts: List<Account>): List<Long> {
        val entities = accounts.map { accountToEntity(it) }
        return accountDao.insertAccounts(entities)
    }

    suspend fun updateAccount(account: Account) {
        val entity = accountToEntity(account)
        accountDao.updateAccount(entity)
    }

    suspend fun updateAccountOrders(accounts: List<Account>) {
        accounts.forEachIndexed { index, account ->
            val updated = account.copy(displayOrder = index)
            accountDao.updateAccount(accountToEntity(updated))
        }
    }

    suspend fun updateHotpCounter(id: Long, newCounter: Long) {
        accountDao.updateHotpCounter(id, newCounter)
    }

    suspend fun deleteAccountById(id: Long) {
        accountDao.deleteAccountById(id)
    }

    suspend fun findDuplicates(issuer: String, accountName: String): List<Account> {
        val entities = accountDao.findDuplicates(issuer, accountName)
        return entities.map { entityToAccount(it) }
    }

    suspend fun insertFolder(name: String, colorHex: String): Long {
        return folderDao.insertFolder(FolderEntity(name = name, colorHex = colorHex))
    }

    suspend fun updateFolder(folder: FolderEntity) {
        folderDao.updateFolder(folder)
    }

    suspend fun deleteFolder(folder: FolderEntity) {
        folderDao.unassignAccountsFromFolder(folder.id)
        folderDao.deleteFolder(folder)
    }

    private fun accountToEntity(account: Account): AccountEntity {
        // Encrypt raw Base32 secret at rest
        val encrypted = KeystoreEncryption.encryptSecret(account.secretBase32)
        return AccountEntity(
            id = account.id,
            accountName = account.accountName,
            issuer = account.issuer,
            secretEncrypted = encrypted,
            type = account.type.name,
            algorithm = account.algorithm.name,
            digits = account.digits,
            period = account.period,
            counter = account.counter,
            folderId = account.folderId,
            iconKey = account.iconKey,
            displayOrder = account.displayOrder,
            createdAt = account.createdAt
        )
    }

    private fun entityToAccount(entity: AccountEntity): Account {
        val decryptedSecret = try {
            KeystoreEncryption.decryptSecret(entity.secretEncrypted)
        } catch (e: Exception) {
            // Fallback if unencrypted string during legacy migration
            entity.secretEncrypted
        }

        return Account(
            id = entity.id,
            accountName = entity.accountName,
            issuer = entity.issuer,
            secretBase32 = decryptedSecret,
            type = try { OtpType.valueOf(entity.type) } catch (e: Exception) { OtpType.TOTP },
            algorithm = HashAlgorithm.fromString(entity.algorithm),
            digits = entity.digits,
            period = entity.period,
            counter = entity.counter,
            folderId = entity.folderId,
            iconKey = entity.iconKey,
            displayOrder = entity.displayOrder,
            createdAt = entity.createdAt
        )
    }
}
