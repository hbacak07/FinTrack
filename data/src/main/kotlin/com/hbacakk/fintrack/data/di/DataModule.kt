package com.hbacakk.fintrack.data.di

import androidx.room.Room
import com.hbacakk.fintrack.data.local.database.FinTrackDatabase
import com.hbacakk.fintrack.data.remote.repository.BudgetRepositoryImpl
import com.hbacakk.fintrack.data.remote.repository.TransactionRepositoryImpl
import com.hbacakk.fintrack.domain.repository.BudgetRepository
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {

    // Room veritabanı — uygulama boyunca tek instance
    single {
        Room.databaseBuilder(
            androidContext(),
            FinTrackDatabase::class.java,
            "fintrack.db",
        )
            .fallbackToDestructiveMigration(true)
            // NOT: production'da migration stratejisi yazılmalı
            // fallbackToDestructiveMigration sadece geliştirme aşamasında
            .build()
    }

    // DAO'lar — database'den alınır
    single { get<FinTrackDatabase>().transactionDao() }
    single { get<FinTrackDatabase>().budgetDao() }

    // Repository implementasyonları
    // Domain interface'i istenince, implementasyonu dön
    single<TransactionRepository> { TransactionRepositoryImpl(get()) }
    single<BudgetRepository> { BudgetRepositoryImpl(get()) }
}