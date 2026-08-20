package com.example.core.trainingengine.model

/**
 * EVOLUTION HUMAN AI — VALUE STATE
 *
 * Explicit state wrapper for metrics and measurements.
 * Absence of value is NEVER treated as 0.0 or empty.
 */
sealed class ValueState<out T> {
    data class Recorded<out T>(val value: T) : ValueState<T>()
    data object NotSpecified : ValueState<Nothing>()
    data object Unknown : ValueState<Nothing>()
    data object NotApplicable : ValueState<Nothing>()

    val isRecorded: Boolean get() = this is Recorded
    fun getOrNull(): T? = (this as? Recorded)?.value
    fun getOrDefault(default: @UnsafeVariance T): T = (this as? Recorded)?.value ?: default

    override fun toString(): String = when (this) {
        is Recorded -> value.toString()
        is NotSpecified -> "NOT_SPECIFIED"
        is Unknown -> "UNKNOWN"
        is NotApplicable -> "NOT_APPLICABLE"
    }

    companion object {
        fun <T> of(value: T?): ValueState<T> = if (value != null) Recorded(value) else NotSpecified
    }
}
