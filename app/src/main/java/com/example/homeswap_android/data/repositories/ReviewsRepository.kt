package com.example.homeswap_android.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ReviewsRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val reviewsCollectionReference: CollectionReference,
) {

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews


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

    fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
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


}
