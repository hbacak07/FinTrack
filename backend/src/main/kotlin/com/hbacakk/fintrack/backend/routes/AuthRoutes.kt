package com.hbacakk.fintrack.backend.routes

import com.hbacakk.fintrack.backend.auth.JwtConfig
import com.hbacakk.fintrack.backend.dto.AuthResponse
import com.hbacakk.fintrack.backend.dto.ErrorResponse
import com.hbacakk.fintrack.backend.dto.LoginRequest
import com.hbacakk.fintrack.backend.dto.RegisterRequest
import com.hbacakk.fintrack.backend.dto.UserDto
import com.hbacakk.fintrack.backend.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes(userRepository: UserRepository) {

    post("/auth/register") {
        val request = call.receive<RegisterRequest>()

        if (userRepository.findByEmail(request.email) != null) {
            call.respond(HttpStatusCode.Conflict, ErrorResponse("Bu e-posta zaten kayıtlı"))
            return@post
        }

        val user = userRepository.create(request.email, request.password, request.fullName)
        val token = JwtConfig.generateToken(user.id, user.email)

        call.respond(
            HttpStatusCode.Created,
            AuthResponse(token, UserDto(user.id, user.email, user.fullName)),
        )
    }

    post("/auth/login") {
        val request = call.receive<LoginRequest>()
        val user = userRepository.findByEmail(request.email)

        if (user == null || !userRepository.verifyPassword(request.password, user.passwordHash)) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("E-posta veya şifre hatalı"))
            return@post
        }

        val token = JwtConfig.generateToken(user.id, user.email)
        call.respond(
            HttpStatusCode.OK,
            AuthResponse(token, UserDto(user.id, user.email, user.fullName)),
        )
    }
}