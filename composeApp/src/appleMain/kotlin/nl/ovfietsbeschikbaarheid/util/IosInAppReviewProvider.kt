package nl.ovfietsbeschikbaarheid.util

import platform.StoreKit.SKStoreReviewController

class IosInAppReviewProvider(
    private val ratingEligibilityService: RatingEligibilityService
): InAppReviewProvider {
    override suspend fun invokeAppReview() {
        SKStoreReviewController.requestReview()
        ratingEligibilityService.onRatingPrompted()
    }
}