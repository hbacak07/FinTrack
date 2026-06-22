package com.hbacakk.fintrack.core.network.util

import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Network/HTTP katmanından gelen ham exception'ları
 * DomainException'a çevirir.
 *
 * Neden bu dönüşüm gerekli?
 * ViewModel veya UseCase katmanı, Retrofit'in HttpException'ını
 * veya OkHttp'nin IOException'ını TANIMAMALI. Çünkü:
 * - Domain katmanı network kütüphanesinden habersiz olmalı
 * - Yarın Retrofit'ten Ktor client'a geçersek, sadece bu dosya değişir
 */
fun Throwable.toDomainException(): DomainException = when (this) {
    is HttpException -> mapHttpException(this)
    is UnknownHostException -> DomainException.NetworkException(
        message = "İnternet bağlantınızı kontrol edin",
        cause = this,
    )
    is SocketTimeoutException -> DomainException.TimeoutException(cause = this)
    is IOException -> DomainException.NetworkException(cause = this)
    is DomainException -> this  // Zaten dönüştürülmüş, olduğu gibi geçir
    else -> DomainException.UnknownException(
        message = message ?: "Beklenmeyen bir hata oluştu",
        cause = this,
    )
}

private fun mapHttpException(exception: HttpException): DomainException =
    when (exception.code()) {
        401 -> DomainException.UnauthorizedException(cause = exception)
        403 -> DomainException.UnauthorizedException(
            message = "Bu işlem için yetkiniz yok",
            cause = exception,
        )
        404 -> DomainException.UnknownException(
            message = "İstenen kaynak bulunamadı",
            cause = exception,
        )
        in 500..599 -> DomainException.ServerException(
            code = exception.code(),
            cause = exception,
        )
        else -> DomainException.UnknownException(
            message = "Sunucu hatası: ${exception.code()}",
            cause = exception,
        )
    }

/**
 * Bir suspend network çağrısını güvenli şekilde sarmalayan yardımcı fonksiyon.
 * Repository implementasyonlarında şu şekilde kullanılır:
 *
 * override suspend fun login(...) = safeApiCall {
 *     apiService.login(request)
 * }
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T,
): Result<T> = try {
    Result.Success(apiCall())
} catch (e: Throwable) {
    Result.Error(e.toDomainException())
}
