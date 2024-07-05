package com.example.homeswap_android.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.ApartmentReview
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.data.models.UserReview
import com.example.homeswap_android.data.repositories.ApartmentRepository
import com.example.homeswap_android.data.repositories.ReviewsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class ReviewsViewModel : ViewModel() {

    private val TAG = "FirebaseApartmentViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reviewsCollectionReference = firestore.collection("reviews")
    private val reviewsRepository =
        ReviewsRepository(auth, storage, reviewsCollectionReference)

    val newAddedReview = reviewsRepository.newAddedReview


    fun resetNewAddedReview() {
        reviewsRepository.resetNewAddedReview()
    }

    fun getUserReviews(userID: String): Query {
        return reviewsRepository.getUserReviews(userID)
    }

    fun getApartmentReviews(apartmentID: String): Query {
        return reviewsRepository.getApartmentReviews(apartmentID)
    }

    fun addReview(review: Review) {
        reviewsRepository.addReview(review)
    }
}