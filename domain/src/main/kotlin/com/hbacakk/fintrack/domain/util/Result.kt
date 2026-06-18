package com.hbacakk.fintrack.domain.util

/**
 * Tüm use case'lerin döndürdüğü tip.
 *
 * Neden Exception fırlatmıyoruz?
 * - Exception'lar Kotlin'de fonksiyon imzasında görünmez, caller unutabilir
 * - Result<T> ile compiler başarı VE hata durumunu ele almaya zorlar
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: DomainException) : Result<Nothing>
    data object Loading : Result<Nothing>
}

/** Başarılı sonucu dönüştür, hata ise olduğu gibi ilet */
inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error   -> this
    is Result.Loading -> this
}

/** Sadece başarılı durumda bir şey yap */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

/** Sadece hata durumunda bir şey yap */
inline fun <T> Result<T>.onError(action: (DomainException) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}

/** Result<T> → T, hata durumunda null döner */
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else              -> null
}