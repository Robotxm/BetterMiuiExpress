package com.moefactory.bettermiuiexpress.base

import com.highcapable.yukihookapi.hook.log.YLog
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
        YLog.error(e = throwable)
        onFetchFailed(throwable)
        Result.failure(throwable)
    }

    emit(data)
}
