package com.hbacakk.fintrack.domain.util

/**
 * Uygulamanın tüm hata tipleri burada tanımlı.
 *
 * sealed class kullanmak, when expression'da tüm hata
 * tiplerini handle etmeyi zorunlu kılar — hiçbiri gözden kaçmaz.
 */
sealed class DomainException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    data class NetworkException(
        override val message: String = "İnternet bağlantısı yok",
        override val cause: Throwable? = null,
    ) : DomainException(message, cause)

    data class TimeoutException(
        override val message: String = "Bağlantı zaman aşımına uğradı",
        override val cause: Throwable? = null,
    ) : DomainException(message, cause)

    data class UnauthorizedException(
        override val message: String = "Oturum süresi doldu",
        override val cause: Throwable? = null,
    ) : DomainException(message, cause)

    data class ServerException(
        val code: Int,
        override val message: String = "Sunucu hatası ($code)",
        override val cause: Throwable? = null,
    ) : DomainException(message, cause)

    data class NotFoundException(
        override val message: String = "İstenen kaynak bulunamadı",
        override val cause: Throwable? = null,
    ) : DomainException(message, cause)

    data class ValidationException(
        val field: String,
        override val message: String,
    ) : DomainException(message)

    data class InsufficientFundsException(
        val available: Double,
        val required: Double,
        override val message: String = "Yetersiz bakiye",
    ) : DomainException(message)

    data class UnknownException(
        override val message: String = "Beklenmeyen bir hata oluştu",
        override val cause: Throwable? = null,
    ) : DomainException(message, cause)
}