package com.hbacakk.fintrack.backend.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * H2 in-memory veritabanı bağlantısı.
 *
 * "in-memory" demek, uygulama her yeniden başladığında
 * veritabanı sıfırlanır — bu, gösteri/öğrenme projesi için
 * idealdir (Docker kurulumu gerektirmez), production'da
 * gerçek bir PostgreSQL/MySQL kullanılır.
 */
object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:fintrack;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver",
        )

        transaction {
            SchemaUtils.create(Users, Transactions, Budgets)
        }
    }
}
