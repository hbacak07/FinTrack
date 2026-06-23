package com.hbacakk.fintrack.backend.routes

import com.hbacakk.fintrack.backend.dto.CreateTransactionRequest
import com.hbacakk.fintrack.backend.dto.TransactionDto
import com.hbacakk.fintrack.backend.repository.TransactionRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.transactionRoutes(transactionRepository: TransactionRepository) {
    authenticate("auth-jwt") {
        route("/transactions") {
            get {
                val userId = call.userId()
                val transactions = transactionRepository.findAllByUser(userId).map { it.toDto() }
                call.respond(HttpStatusCode.OK, transactions)
            }

            post {
                val userId = call.userId()
                val request = call.receive<CreateTransactionRequest>()

                val created =
                    transactionRepository.create(
                        userId = userId,
                        amount = request.amount,
                        type = request.type,
                        category = request.category,
                        description = request.description,
                        date = request.date,
                        accountId = request.accountId,
                    )

                call.respond(HttpStatusCode.Created, created.toDto())
            }

            delete("/{id}") {
                val userId = call.userId()
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val deleted = transactionRepository.deleteById(id, userId)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private fun com.hbacakk.fintrack.backend.repository.TransactionRecord.toDto() =
    TransactionDto(
        id = id,
        amount = amount,
        type = type,
        category = category,
        description = description,
        date = date,
        accountId = accountId,
    )
