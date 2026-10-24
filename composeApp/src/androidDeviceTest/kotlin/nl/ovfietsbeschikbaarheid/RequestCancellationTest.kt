package nl.ovfietsbeschikbaarheid

import io.ktor.client.HttpClient
import io.ktor.client.request.prepareGet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test

class RequestCancellationTest {

    @Test
    fun cancellingOnTheMainThreadWhileTheResponseBodyIsUnreadDoesNotCrash() = runBlocking {
        HttpClient().use { httpClient ->
            val responseReceived = CompletableDeferred<Unit>()
            val requestJob = launch(Dispatchers.Main) {
                httpClient.prepareGet("https://storage.googleapis.com/ov-fiets-updates/locations.json").execute {
                    responseReceived.complete(Unit)
                    awaitCancellation()
                }
            }

            responseReceived.await()
            withContext(Dispatchers.Main) {
                requestJob.cancel()
            }
            requestJob.join()
        }
    }
}
