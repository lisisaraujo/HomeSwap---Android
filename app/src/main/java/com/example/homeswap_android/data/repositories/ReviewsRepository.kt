package com.example.homeswap_android.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.ApartmentReview
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.data.models.UserReview
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

    private val _newAddedReview = MutableLiveData<Review>()
    val newAddedReview: LiveData<Review> = _newAddedReview


    fun getUserReviews(userID: String): Query {
        return reviewsCollectionReference
            .whereEqualTo("reviewType", "user")
            .whereEqualTo("userID", userID)
    }

    fun getApartmentReviews(apartmentID: String): Query {
        return reviewsCollectionReference
            .whereEqualTo("reviewType", "apartment")
            .whereEqualTo("apartmentID", apartmentID)
    }

    fun addReview(review: Review) {
        val currentUser = auth.currentUser
        currentUser?.let {
            val reviewMap = when (review) {
                is ApartmentReview -> mapOf(
                    "reviewType" to "apartment",
                    "apartmentID" to review.apartmentID,
                    "reviewID" to review.reviewID,
                    "reviewerID" to review.reviewerID,
                    "reviewerName" to review.reviewerName,
                    "review" to review.review,
                    "date" to review.date,
                    "rating" to review.rating,
                    "reviewerProfilePic" to review.reviewerProfilePic
                )
                is UserReview -> mapOf(
                    "reviewType" to "user",
                    "userID" to review.userID,
                    "reviewID" to review.reviewID,
                    "reviewerID" to review.reviewerID,
                    "reviewerName" to review.reviewerName,
                    "review" to review.review,
                    "date" to review.date,
                    "rating" to review.rating,
                    "reviewerProfilePic" to review.reviewerProfilePic
                )
                else -> throw IllegalArgumentException("Unsupported review type")
            }

            reviewsCollectionReference.add(reviewMap)
                .addOnSuccessListener { documentReference ->
                    val updatedReview = when (review) {
                        is ApartmentReview -> review.copy(reviewID = documentReference.id)
                        is UserReview -> review.copy(reviewID = documentReference.id)
                        else -> throw IllegalArgumentException("Unsupported review type")
                    }
                    documentReference.update("reviewID", documentReference.id)
                        .addOnSuccessListener {
                            _newAddedReview.postValue(updatedReview)
                            Log.d(TAG, updatedReview.review)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error updating review ID: ${exception.message}")
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error adding review: ${exception.message}")
                }
        }
    }
    }