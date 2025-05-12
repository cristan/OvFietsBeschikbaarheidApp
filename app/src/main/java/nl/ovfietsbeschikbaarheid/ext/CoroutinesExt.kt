package nl.ovfietsbeschikbaarheid.ext

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

suspend fun <T> Deferred<T>.tryAwait(timeoutMillis: Long): T? {
    return try {
        withTimeout(timeoutMillis) {
            this@tryAwait.await()
        }
    } catch (e: TimeoutCancellationException) {
        null
    }
}