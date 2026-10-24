package nl.ovfietsbeschikbaarheid.util

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.IOException
import nl.ovfietsbeschikbaarheid.KtorApiClient
import nl.ovfietsbeschikbaarheid.model.AppRelease
import nl.ovfietsbeschikbaarheid.repository.UsageStatisticsSettingRepository
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
    private val settingRepository: UsageStatisticsSettingRepository,
) {
    private val scope = CoroutineScope(SupervisorJob())
    private val sentWarnings = mutableSetOf<String>()
    private val sentWarningsMutex = Mutex()

    /**
     * Counts the app start and passes every warning that is logged from then on to [onWarningLogged].
     */
    fun onAppStarted() {
        Logger.addLogWriter(WarningLogWriter(this))
        count(UsageEvent.AppStart)
    }

    fun onDetailsOpened() {
        count(UsageEvent.DetailsOpened)
    }

    fun onWarningLogged(message: String) {
        sendIfEnabled {
            val isFirstTime = sentWarningsMutex.withLock { sentWarnings.add(message) }
            if (isFirstTime) {
                client.addWarning(message, appRelease)
            }
        }
    }

    private fun count(event: UsageEvent) {
        sendIfEnabled {
            client.incrementUsageCount(usageCountId(Clock.System.now(), appRelease, event))
        }
    }

    private fun sendIfEnabled(request: suspend () -> Unit) {
        scope.launch {
            if (!settingRepository.isEnabled.first()) {
                return@launch
            }
            try {
                request()
            } catch (e: ResponseException) {
                Logger.i(e) { "Failed to send usage statistics" }
            } catch (e: IOException) {
                Logger.i(e) { "Failed to send usage statistics" }
            }
        }
    }
}

private class WarningLogWriter(private val usageStatisticsSender: UsageStatisticsSender) : LogWriter() {
    override fun isLoggable(tag: String, severity: Severity) = severity == Severity.Warn

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        usageStatisticsSender.onWarningLogged(message)
    }
}
