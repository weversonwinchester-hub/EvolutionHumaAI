package com.example.core.error

sealed class AppError(open val message: String, open val cause: Throwable? = null) {
    data class AuthenticationError(override val message: String) : AppError(message)
    data class AuthorizationError(override val message: String) : AppError(message)
    data class ValidationError(override val message: String) : AppError(message)
    data class SecurityViolation(override val message: String) : AppError(message)
    data class NotFoundError(override val message: String) : AppError(message)
    data class DatabaseError(override val message: String, override val cause: Throwable? = null) : AppError(message, cause)
    data class EngineNotImplementedError(override val message: String) : AppError(message)
    data class ImmutableHistoryViolation(override val message: String = "Tentativa de alteração direta em histórico imutável detectada e bloqueada.") : AppError(message)
    data class UnauthorizedStateMutation(override val message: String = "Apenas o Core possui autoridade para atualizar o estado oficial do usuário.") : AppError(message)
    data class InvalidEvidence(override val message: String) : AppError(message)
    data class ComputationError(override val message: String) : AppError(message)
}

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }
}
