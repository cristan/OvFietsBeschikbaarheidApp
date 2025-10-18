package nl.ovfietsbeschikbaarheid.util

import androidx.activity.ComponentActivity
import co.touchlab.kermit.Logger
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.lang.ref.WeakReference
import kotlin.coroutines.resume

class AndroidInAppReviewProvider(
    private val ratingEligibilityService: RatingEligibilityService
) : InAppReviewProvider {
    private lateinit var _activity: WeakReference<ComponentActivity>
    fun setActivity(activity: ComponentActivity) {
        this._activity = WeakReference(activity)
    }

    override suspend fun invokeAppReview() {
        val activity = _activity.get()
        if (activity == null) {
            Logger.e("Cannot launch review: activity is null")
            return
        }
        val manager = ReviewManagerFactory.create(activity)
        val reviewInfo = manager.awaitReviewInfo()
        if (reviewInfo == null) {
            return
        }
        Logger.d("Launching review flow")
        manager.launchReviewFlow(activity, reviewInfo).await()
        ratingEligibilityService.onRatingPrompted()
    }
}

private suspend fun ReviewManager.awaitReviewInfo(): ReviewInfo? {
    return suspendCancellableCoroutine { cont ->
        requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                Logger.e(task.exception) { "Failed to request review flow" }
                cont.resume(null)
            }
        }
    }
}