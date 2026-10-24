package nl.ovfietsbeschikbaarheid.util

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.IOException
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.model.AppRelease
import kotlin.time.Clock
import kotlin.time.Instant

enum class UsageEvent(val id: String) {
    AppStart("app_start"),
    DetailsOpened("details_opened"),
}

fun usageCountId(now: Instant, appRelease: AppRelease, event: UsageEvent): String {
    val utcDate = now.toLocalDateTime(TimeZone.UTC).date
    return "${utcDate}_${appRelease.platform}_${appRelease.versionName}_${event.id}"
}

class UsageStatisticsSender(
    private val client: KtorApiClient,
    private val appRelease: AppRelease,
) {
    private val scope = CoroutineScope(SupervisorJob())

    fun onAppStarted() {
        count(UsageEvent.AppStart)
    }

    fun onDetailsOpened() {
        count(UsageEvent.DetailsOpened)
    }

    private fun count(event: UsageEvent) {
        scope.launch {
            try {
                client.incrementUsageCount(usageCountId(Clock.System.now(), appRelease, event))
            } catch (e: ResponseException) {
                Logger.i(e) { "Failed to send usage statistics" }
            } catch (e: IOException) {
                Logger.i(e) { "Failed to send usage statistics" }
            }
        }
    }
}
