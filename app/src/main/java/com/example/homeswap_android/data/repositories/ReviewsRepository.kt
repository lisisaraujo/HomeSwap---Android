package com.example.homeswap_android.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import kotlin.math.roundToInt

class ReviewsRepository(
    private val auth: FirebaseAuth,
    private val reviewsCollectionReference: CollectionReference,
    private val apartmentsCollectionReference: CollectionReference,
    private val usersCollectionReference: CollectionReference


) {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _apartmentReviews = MutableLiveData<List<Review>>()
    val apartmentReviews: LiveData<List<Review>> = _apartmentReviews

    private val _userReviews = MutableLiveData<List<Review>>()
    val userReviews: LiveData<List<Review>> = _userReviews


    fun getUserReviews(userID: String): Query {
        return reviewsCollectionReference
            .whereEqualTo("reviewType", "user")
            .whereEqualTo("destinationID", userID)
    }

    fun getApartmentReviews(apartmentID: String): Query {
        return reviewsCollectionReference
            .whereEqualTo("reviewType", "apartment")
            .whereEqualTo("destinationID", apartmentID)
    }


    fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val reviewMap =
                mapOf(
                    "reviewType" to review.reviewType,
                    "destinationID" to review.destinationID,
                    "reviewID" to review.reviewID,
                    "reviewerID" to review.reviewerID,
                    "reviewerName" to review.reviewerName,
                    "review" to review.review,
                    "date" to review.date,
                    "rating" to review.rating,
                    "reviewerProfilePic" to review.reviewerProfilePic
                )

            reviewsCollectionReference.add(reviewMap)
                .addOnSuccessListener { documentReference ->
                    documentReference.update("reviewID", documentReference.id)
                        .addOnSuccessListener {
                            Log.d(TAG, "Review added successfully with ID: ${documentReference.id}")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error updating review ID: ${exception.message}")
                            onFailure(exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error adding review: ${exception.message}")
                    onFailure(exception)
                }
        }
    }

    fun updateRating(ratingObjectId: String, reviewType: String) {
        val reviewsQuery = when (reviewType) {
            "user" -> getUserReviews(ratingObjectId)
            "apartment" -> getApartmentReviews(ratingObjectId)
            else -> throw IllegalArgumentException("Invalid review type: $reviewType")
        }

        reviewsQuery.get()
            .addOnSuccessListener { querySnapshot ->
                val reviews = querySnapshot.toObjects(Review::class.java)
                var totalRating = 0f
                var validReviewCount = 0

                for (review in reviews) {
                    review.rating?.let {
                        totalRating += it
                        validReviewCount++
                    }
                }

                val averageRating = if (validReviewCount > 0) {
                    (totalRating / validReviewCount * 10).roundToInt() / 10f
                } else {
                    0f
                }

                val collectionReference = when (reviewType) {
                    "user" -> usersCollectionReference
                    "apartment" -> apartmentsCollectionReference
                    else -> throw IllegalArgumentException("Invalid review type: $reviewType")
                }

                collectionReference.document(ratingObjectId)
                    .update(
                        mapOf(
                            "rating" to averageRating,
                            "reviewsCount" to validReviewCount
                        )
                    )
                    .addOnSuccessListener {
                        Log.d("Repository", "$reviewType rating updated successfully. New rating: $averageRating")
                    }
                    .addOnFailureListener { e: Exception ->
                        Log.e("Repository", "Error updating $reviewType rating", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Repository", "Error fetching $reviewType reviews", e)
            }
    }

}
