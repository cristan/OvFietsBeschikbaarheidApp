package nl.ovfietsbeschikbaarheid.util

interface InAppReviewProvider {
    suspend fun invokeAppReview()
}