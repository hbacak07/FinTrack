package com.hbacakk.fintrack.backend.routes

import com.hbacakk.fintrack.backend.dto.BudgetDto
import com.hbacakk.fintrack.backend.dto.CreateBudgetRequest
import com.hbacakk.fintrack.backend.repository.BudgetRecord
import com.hbacakk.fintrack.backend.repository.BudgetRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.budgetRoutes(budgetRepository: BudgetRepository) {
    authenticate("auth-jwt") {
        route("/budgets") {
            get {
                val userId = call.userId()
                val budgets = budgetRepository.findAllByUser(userId).map { it.toDto() }
                call.respond(HttpStatusCode.OK, budgets)
            }

            post {
                val userId = call.userId()
                val request = call.receive<CreateBudgetRequest>()

                val created =
                    budgetRepository.create(
                        userId = userId,
                        name = request.name,
                        limit = request.limit,
                        category = request.category,
                        period = request.period,
                        startDate = request.startDate,
                        endDate = request.endDate,
                    )

                call.respond(HttpStatusCode.Created, created.toDto())
            }
        }
    }
}

private fun BudgetRecord.toDto() =
    BudgetDto(
        id = id,
        name = name,
        limit = limit,
        spent = spent,
        category = category,
        period = period,
        startDate = startDate,
        endDate = endDate,
    )
