package com.hbacakk.fintrack.backend.repository

import com.hbacakk.fintrack.backend.db.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

data class UserRecord(
    val id: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
)

class UserRepository {
    fun create(
        email: String,
        password: String,
        fullName: String,
    ): UserRecord {
        val id = UUID.randomUUID().toString()
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

        transaction {
            Users.insert {
                it[Users.id] = id
                it[Users.email] = email
                it[Users.passwordHash] = passwordHash
                it[Users.fullName] = fullName
                it[Users.createdAt] = System.currentTimeMillis()
            }
        }

        return UserRecord(id, email, passwordHash, fullName)
    }

    fun findByEmail(email: String): UserRecord? =
        transaction {
            Users
                .selectAll()
                .where { Users.email eq email }
                .map {
                    UserRecord(
                        id = it[Users.id],
                        email = it[Users.email],
                        passwordHash = it[Users.passwordHash],
                        fullName = it[Users.fullName],
                    )
                }.singleOrNull()
        }

    fun verifyPassword(
        rawPassword: String,
        hash: String,
    ): Boolean = BCrypt.checkpw(rawPassword, hash)
}
