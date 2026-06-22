package com.hbacakk.fintrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hbacakk.fintrack.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): Room'un veritabanı sorgu arayüzü.
 *
 * Neden Flow döndürüyoruz?
 * Room, Flow ile çalışırken veritabanı değiştiğinde
 * otomatik olarak yeni veriyi yayar. UI her zaman
 * güncel veriyi görür, manuel refresh gerekmez.
 * Bu, "Single Source of Truth" pattern'inin temelidir.
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun observeByAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun observeByType(type: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%Y', date/1000, 'unixepoch') = :year 
        AND strftime('%m', date/1000, 'unixepoch') = :month
        ORDER BY date DESC
    """)
    fun observeByMonth(year: String, month: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)
}
