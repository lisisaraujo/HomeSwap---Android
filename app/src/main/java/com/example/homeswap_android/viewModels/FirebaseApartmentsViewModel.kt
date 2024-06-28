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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class FirebaseApartmentsViewModel : ViewModel() {
    private val TAG = "FirebaseApartmentViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val apartmentsCollectionReference = firestore.collection("apartments")
    private val apartmentRepository = ApartmentRepository(auth, storage, apartmentsCollectionReference)

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

    fun addApartment(apartment: Apartment) {
        apartmentRepository.addApartment(apartment) { updatedApartment ->
            if (updatedApartment != null) {
                getApartments()
            } else {
                Log.e(TAG, "Error adding apartment")
            }
        }
    }

    fun uploadApartmentImage(uri: Uri, apartmentID: String) {
        apartmentRepository.uploadApartmentImage(uri, apartmentID) { imageUrl ->
            if (imageUrl != null) {
                updateApartmentPicture(apartmentID, imageUrl)
            } else {
                Log.e(TAG, "Error uploading apartment image")
            }
        }
    }

    fun updateApartmentPicture(apartmentID: String, imageUrl: String) {
        apartmentRepository.updateApartmentPicture(apartmentID, imageUrl) { success ->
            if (!success) {
                Log.e(TAG, "Error updating apartment picture")
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
        apartmentRepository.updateApartment(apartment) { success ->
            if (success) {
                getApartments()
            } else {
                Log.e(TAG, "Error updating apartment")
            }
        }
    }

    fun saveAdditionalDetails(
        rooms: Int,
        maxGuests: Int,
        typeOfHome: String,
        petsAllowed: Boolean,
        homeOffice: Boolean,
        hasWifi: Boolean
    ) {
        val currentApt = currentApartment.value ?: run {
            Log.e(TAG, "Current apartment is null, can't update")
            return
        }

        val updatedApartment = currentApt.copy(
            rooms = rooms,
            maxGuests = maxGuests,
            typeOfHome = typeOfHome,
            petsAllowed = petsAllowed,
            homeOffice = homeOffice,
            hasWifi = hasWifi
        )

        updateApartment(updatedApartment)
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
