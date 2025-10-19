package nl.ovfietsbeschikbaarheid.util

import platform.StoreKit.SKStoreReviewController

class IosInAppReviewProvider: InAppReviewProvider {
    override suspend fun invokeAppReview() {
        SKStoreReviewController.requestReview()
    }
}