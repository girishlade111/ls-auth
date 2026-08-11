package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts ORDER BY displayOrder ASC, id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE folderId = :folderId ORDER BY displayOrder ASC, id ASC")
    fun getAccountsByFolder(folderId: Long): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>): List<Long>

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET counter = :newCounter WHERE id = :id")
    suspend fun updateHotpCounter(id: Long, newCounter: Long)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("SELECT * FROM accounts WHERE LOWER(issuer) = LOWER(:issuer) AND LOWER(accountName) = LOWER(:accountName)")
    suspend fun findDuplicates(issuer: String, accountName: String): List<AccountEntity>
}
