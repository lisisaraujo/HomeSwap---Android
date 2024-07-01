package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.repositories.ApartmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch


class FirebaseApartmentsViewModel : ViewModel() {
    private val TAG = "FirebaseApartmentViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val apartmentsCollectionReference = firestore.collection("apartments")
    private val apartmentRepository =
        ApartmentRepository(auth, storage, apartmentsCollectionReference)

    val apartments = apartmentRepository.apartments
    val currentApartment = apartmentRepository.currentApartment
    val likedApartments = apartmentRepository.likedApartments
    val apartmentsBySearch = apartmentRepository.apartmentsBySearch

    init {
        getApartments()
        loadLikedApartments()
    }


    fun getApartmentDocumentReference(apartmentId: String): DocumentReference {
        return apartmentRepository.getApartmentDocumentReference(apartmentId)
    }

    fun getApartments() {
        apartmentRepository.getApartments()
    }

    fun getApartment(apartmentID: String) {
        apartmentRepository.getApartment(apartmentID)
    }

    fun getUserApartments(userID: String): Query {
        return apartmentRepository.getUserApartments(userID)
    }

    fun getApartmentPictures(apartmentID: String, userID: String): LiveData<List<String>> {
        return apartmentRepository.getApartmentPictures(apartmentID, userID)
    }

    fun getApartmentFirstPicture(apartmentID: String, userID: String): LiveData<String?> {
        val result = MutableLiveData<String?>()
        viewModelScope.launch {
            result.value = apartmentRepository.getApartmentFirstPicture(apartmentID, userID).toString()
        }
        return result
    }

    fun uploadApartmentImages(uris: List<Uri>, apartmentID: String) {
        viewModelScope.launch {
            apartmentRepository.uploadApartmentImages(uris, apartmentID) { downloadUrls ->
                if (downloadUrls.isNotEmpty()) {
                    // all images uploaded successfully
                    // update the apartment document with these URLs
                    updateApartmentWithImageUrls(apartmentID, downloadUrls)
                } else {
                    Log.e(TAG, "Failed to upload one or more images")
                }
            }
        }
    }

    private fun updateApartmentWithImageUrls(apartmentID: String, imageUrls: List<String>) {
        apartmentRepository.updateApartmentImages(apartmentID, imageUrls) { success ->
            if (success) {
                Log.d(TAG, "Successfully updated apartment with new image URLs")
            } else {
                Log.e(TAG, "Failed to update apartment with new image URLs")
            }
        }
    }



    fun deleteApartment(apartmentID: String) {
        apartmentRepository.deleteApartment(apartmentID) { success ->
            if (success) {
                auth.currentUser?.uid?.let { userId ->
                    getUserApartments(userId)
                } ?: run {
                    Log.e(TAG, "User is not authenticated")
                }
            } else {
                Log.e(TAG, "Error deleting apartment")
            }
        }
    }

    fun toggleLike(apartment: Apartment) {
        val updatedApartment = apartment.copy(liked = !apartment.liked)
        updateApartment(updatedApartment)
    }

    fun updateApartment(apartment: Apartment) {
        apartment.let {
            apartmentRepository.updateApartment(apartment)
        }
    }


fun loadLikedApartments() {
    apartmentRepository.getLikedApartments()
}

fun clearSearch() {
    apartmentRepository.clearSearch()
}

fun searchApartments(
    city: String? = null,
    country: String? = null,
    startDate: String? = null,
    endDate: String? = null
) {
    apartmentRepository.searchApartments(city, country, startDate, endDate)
}
}
