package com.hbacakk.fintrack.backend

import com.hbacakk.fintrack.backend.auth.JwtConfig
import com.hbacakk.fintrack.backend.db.DatabaseFactory
import com.hbacakk.fintrack.backend.repository.BudgetRepository
import com.hbacakk.fintrack.backend.repository.TransactionRepository
import com.hbacakk.fintrack.backend.repository.UserRepository
import com.hbacakk.fintrack.backend.routes.authRoutes
import com.hbacakk.fintrack.backend.routes.budgetRoutes
import com.hbacakk.fintrack.backend.routes.transactionRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    val userRepository = UserRepository()
    val transactionRepository = TransactionRepository()
    val budgetRepository = BudgetRepository()

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(CallLogging)

    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowHeader("Authorization")
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, "Sunucu hatası: ${cause.message}")
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId != null) {
                    io.ktor.server.auth.jwt
                        .JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    routing {
        authRoutes(userRepository)
        transactionRoutes(transactionRepository)
        budgetRoutes(budgetRepository)
    }
}
