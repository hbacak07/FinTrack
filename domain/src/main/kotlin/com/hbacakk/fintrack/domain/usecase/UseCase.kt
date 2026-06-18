package com.hbacakk.fintrack.domain.usecase

import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Tek sonuç döndüren use case'ler için taban sınıf.
 *
 * dispatcher injection ile test edilebilir yapar
 * (test'te TestDispatcher, production'da IO Dispatcher)
 * invoke operator: useCase(params) şeklinde çağrılabilir
 */
abstract class UseCase<in Params, out T>(
    private val dispatcher: CoroutineDispatcher,
) {
    protected abstract suspend fun execute(params: Params): Result<T>

    suspend operator fun invoke(params: Params): Result<T> =
        withContext(dispatcher) {
            execute(params)
        }
}

/** Parametre almayan use case'ler için. */
abstract class NoParamsUseCase<out T>(
    private val dispatcher: CoroutineDispatcher,
) {
    protected abstract suspend fun execute(): Result<T>

    suspend operator fun invoke(): Result<T> =
        withContext(dispatcher) {
            execute()
        }
}

/**
 * Flow döndüren use case'ler için.
 * catch: upstream'den gelen exception'ları yakalar,
 * Result.Error'a dönüştürür — UI asla ham exception görmez.
 */
abstract class FlowUseCase<in Params, out T>(
    private val dispatcher: CoroutineDispatcher,
) {
    protected abstract fun execute(params: Params): Flow<Result<T>>

    operator fun invoke(params: Params): Flow<Result<T>> =
        execute(params)
            .flowOn(dispatcher)
            .catch { throwable ->
                emit(
                    Result.Error(
                        DomainException.UnknownException(
                            message = throwable.message ?: "Beklenmeyen hata",
                            cause = throwable,
                        )
                    )
                )
            }
}