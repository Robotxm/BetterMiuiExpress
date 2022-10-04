package com.moefactory.bettermiuiexpress.base

import kotlinx.coroutines.flow.flow

@Suppress("FunctionName")
inline fun <RequestType> NetworkBoundResource(
    crossinline fetch: suspend () -> RequestType,
    crossinline onFetchFailed: (Throwable) -> Unit = { }
) = flow {
    val data = try {
        val result = fetch()
        Result.success(result)
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
        onFetchFailed(throwable)
        Result.failure(throwable)
    }

    emit(data)
}
