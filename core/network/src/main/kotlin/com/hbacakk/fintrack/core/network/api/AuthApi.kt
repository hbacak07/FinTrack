package com.hbacakk.fintrack.core.network.api

import com.hbacakk.fintrack.core.network.model.LoginRequest
import com.hbacakk.fintrack.core.network.model.LoginResponse
import com.hbacakk.fintrack.core.network.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Auth endpoint'leri.
 *
 * @Headers("No-Auth: true") -> AuthInterceptor bu isteklere
 * token eklemeyecek, çünkü login/register'da henüz token yok.
 *
 * NOT: refresh token akışı bu projede bilinçli olarak
 * uygulanmadı (backend'i sade tutma kararı, Adım 12).
 */
interface AuthApi {

    @POST("auth/login")
    @Headers("No-Auth: true")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    @Headers("No-Auth: true")
    suspend fun register(@Body request: RegisterRequest): LoginResponse
}
