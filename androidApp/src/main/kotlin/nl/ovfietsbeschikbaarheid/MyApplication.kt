package nl.ovfietsbeschikbaarheid

import android.app.Application
import nl.ovfietsbeschikbaarheid.di.androidAppModule
import nl.ovfietsbeschikbaarheid.ext.createActivityLifecycleObserver
import nl.ovfietsbeschikbaarheid.util.AndroidInAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.AndroidPlatformLocationHelper
import nl.ovfietsbeschikbaarheid.util.UsageStatisticsSender
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setUpKoin()

        val usageStatisticsSender: UsageStatisticsSender by inject()
        usageStatisticsSender.onAppStarted()

        val locationHelper: AndroidPlatformLocationHelper by inject()
        val androidInAppReviewProvider: AndroidInAppReviewProvider by inject()

        registerActivityLifecycleCallbacks(createActivityLifecycleObserver { activity ->
            locationHelper.setActivity(activity)
            androidInAppReviewProvider.setActivity(activity)
        })
    }

    private fun setUpKoin() {
        startKoin {
            androidContext(this@MyApplication)
            modules(androidAppModule())
        }
    }
}
