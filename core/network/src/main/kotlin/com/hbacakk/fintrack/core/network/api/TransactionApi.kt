package com.hbacakk.fintrack.core.network.api

import com.hbacakk.fintrack.core.network.model.CreateTransactionRequest
import com.hbacakk.fintrack.core.network.model.TransactionDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Transaction endpoint'leri — JWT korumalı (AuthInterceptor otomatik
 * Authorization header ekler, @Headers("No-Auth: true") YOK).
 */
interface TransactionApi {

    @GET("transactions")
    suspend fun getTransactions(): List<TransactionDto>

    @POST("transactions")
    suspend fun createTransaction(@Body request: CreateTransactionRequest): TransactionDto

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String)
}
