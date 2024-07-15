package com.example.homeswap_android.viewModels

import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.data.repositories.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ReviewsViewModel : ViewModel() {

    private val TAG = "FirebaseApartmentViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val reviewsCollectionReference = firestore.collection("reviews")
    private val apartmentsCollectionReference = firestore.collection("apartments")
    private val usersCollectionReference = firestore.collection("users")

    private val reviewsRepository =
        ReviewsRepository(
            auth,
            reviewsCollectionReference,
            apartmentsCollectionReference,
            usersCollectionReference
        )

    fun getUserReviews(userID: String): Query {
        return reviewsRepository.getUserReviews(userID)
    }

    fun getApartmentReviews(apartmentID: String): Query {
        return reviewsRepository.getApartmentReviews(apartmentID)
    }

    fun addReview(review: Review, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        reviewsRepository.addReview(review, onSuccess, onFailure)
        reviewsRepository.updateRating(review.destinationID, review.reviewType)
    }
}