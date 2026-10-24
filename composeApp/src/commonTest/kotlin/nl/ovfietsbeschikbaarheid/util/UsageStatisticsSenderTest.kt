package nl.ovfietsbeschikbaarheid.util

import nl.ovfietsbeschikbaarheid.model.AppRelease
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class UsageStatisticsSenderTest {

    @Test
    fun `the date in a usage count id is the UTC date`() {
        // 00:30 on 24 October in the Netherlands
        val now = Instant.parse("2026-10-23T22:30:00Z")

        val usageCountId = usageCountId(now, AppRelease(platform = "android", versionName = "3.7.2"), UsageEvent.AppStart)

        assertEquals("2026-10-23_android_3.7.2_app_start", usageCountId)
    }
}
