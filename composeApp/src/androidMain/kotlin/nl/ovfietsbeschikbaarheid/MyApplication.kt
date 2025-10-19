package nl.ovfietsbeschikbaarheid

import android.app.Application
import nl.ovfietsbeschikbaarheid.di.androidModule
import nl.ovfietsbeschikbaarheid.di.appModule
import nl.ovfietsbeschikbaarheid.ext.createActivityLifecycleObserver
import nl.ovfietsbeschikbaarheid.util.AndroidInAppReviewProvider
import nl.ovfietsbeschikbaarheid.util.AndroidPlatformLocationHelper
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setUpKoin()

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
            modules(
                appModule(),
                androidModule()
            )
        }
    }
}
