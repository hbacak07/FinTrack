package com.hbacakk.fintrack.backend.routes

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun ApplicationCall.userId(): String {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("userId")?.asString()
        ?: throw IllegalStateException("userId claim bulunamadı")
}