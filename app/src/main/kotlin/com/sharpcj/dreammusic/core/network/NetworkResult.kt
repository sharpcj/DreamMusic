package com.sharpcj.dreammusic.core.network

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Failure(val message: String, val cause: Throwable? = null) : NetworkResult<Nothing>
}

inline fun <T> runNetworkCatching(block: () -> T): NetworkResult<T> = try {
    NetworkResult.Success(block())
} catch (throwable: Throwable) {
    NetworkResult.Failure(
        message = throwable.message?.takeIf { it.isNotBlank() } ?: throwable::class.java.simpleName,
        cause = throwable,
    )
}
