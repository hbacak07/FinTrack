package com.hbacakk.fintrack.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hbacakk.fintrack.data.local.dao.BudgetDao
import com.hbacakk.fintrack.data.local.dao.TransactionDao
import com.hbacakk.fintrack.data.local.entity.AccountEntity
import com.hbacakk.fintrack.data.local.entity.BudgetEntity
import com.hbacakk.fintrack.data.local.entity.TransactionEntity

/**
 * Room veritabanı — uygulamanın yerel veri deposu.
 *
 * version: Şema değiştiğinde artırılır. Room, migration
 * stratejisi olmadan version değişirse çalışmaz.
 * Geliştirme sırasında fallbackToDestructiveMigration()
 * kullanılabilir ama production'da asla!
 *
 * exportSchema = true: Room, şema dosyasını
 * assets/ klasörüne yazar. Bu dosyayı version control'e
 * eklemek, şema geçmişini takip etmeyi sağlar.
 */
@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        AccountEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FinTrackDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}
