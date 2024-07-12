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
    val searchCompletedEvent = apartmentRepository.searchCompletedEvent
    val loadingApartments = apartmentRepository.loadingApartments


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

    fun getApartment(apartmentID: String): LiveData<Apartment> {
        return apartmentRepository.getApartment(apartmentID)
    }

    fun getUserApartments(userID: String): Query {
        return apartmentRepository.getUserApartments(userID)
    }

    fun getApartmentPictures(apartmentID: String, userID: String): LiveData<List<String>> {
        return apartmentRepository.getApartmentPictures(apartmentID, userID)
    }

    fun uploadApartmentImages(uris: List<Uri>, apartmentID: String): LiveData<List<String>> {
        val resultLiveData = MutableLiveData<List<String>>()
        apartmentRepository.uploadApartmentImages(uris, apartmentID) { urls ->
            resultLiveData.postValue(urls)
        }
        return resultLiveData
    }

    private fun updateApartmentWithImageUrls(apartmentID: String, imageUrls: List<String>) {
        apartmentRepository.updateApartmentImageURLs(apartmentID, imageUrls) { success ->
            if (success) {
                Log.d(TAG, "Successfully updated apartment with new image URLs")

            } else {
                Log.e(TAG, "Failed to update apartment with new image URLs")
            }
        }
    }

    fun deleteApartment(
        apartmentID: String,
        userID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        apartmentRepository.deleteApartment(
            apartmentID,
            userID,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
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
        startDate: String? = null,
        endDate: String? = null,
        typeOfHome: String? = null,
        amenities: List<String>? = null,
        rooms: Int? = null,
        maxGuests: Int? = null
    )  {
        viewModelScope.launch {
            apartmentRepository.searchApartments(city, startDate, endDate, typeOfHome, amenities, rooms, maxGuests)
        }
    }
}
