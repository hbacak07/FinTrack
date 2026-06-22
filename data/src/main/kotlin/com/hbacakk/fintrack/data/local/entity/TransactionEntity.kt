package com.hbacakk.fintrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity: veritabanındaki "transactions" tablosunu temsil eder.
 *
 * Domain modeli (Transaction) ile bu sınıf AYRI tutulur.
 * Neden?
 * - Domain modeli iş mantığı içerebilir (computed property'ler)
 * - Entity'de @PrimaryKey, @ColumnInfo gibi Room annotation'ları olabilir
 * - İkisi ayrı tutulursa, Room şemasını değiştirmek domain'i etkilemez
 *
 * Bu iki katman arasındaki dönüşüm Mapper fonksiyonlarıyla yapılır.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: String,          // TransactionType enum'un string karşılığı
    val category: String,      // Category enum'un string karşılığı
    val description: String,
    val date: Long,
    val accountId: String,
    val isRecurring: Boolean,
    val isSynced: Boolean = false,  // Offline-first için: sunucuyla sync edildi mi?
    val createdAt: Long = System.currentTimeMillis(),
)
