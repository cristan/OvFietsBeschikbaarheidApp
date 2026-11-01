package nl.ovfietsbeschikbaarheid.ext

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

suspend fun <T> Deferred<T>.tryAwait(timeoutMillis: Long): T? {
    return try {
        withTimeout(timeoutMillis.milliseconds) {
            this@tryAwait.await()
        }
    } catch (e: TimeoutCancellationException) {
        null
    }
}