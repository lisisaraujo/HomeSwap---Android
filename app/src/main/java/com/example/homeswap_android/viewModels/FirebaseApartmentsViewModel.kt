package com.example.homeswap_android.viewModels

import android.net.Uri
import android.system.Os.remove
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _currentFilters = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val currentFilters: StateFlow<Map<String, Any?>> = _currentFilters

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
        viewModelScope.launch {
            try {
                apartmentRepository.updateApartment(apartment)
            } catch (e: Exception) {
               Log.d(TAG, e.toString())
            }
        }
    }


    fun loadLikedApartments() {
        apartmentRepository.getLikedApartments()
    }


    fun clearSearch() {
        viewModelScope.launch {
            _currentFilters.value = emptyMap()
            apartmentRepository.clearSearch()
        }
    }

    fun searchApartments(filters: Map<String, Any?>) {
        viewModelScope.launch {
            _currentFilters.value = filters
            performSearch()
        }
    }

    fun removeFilter(filterKey: String) {
        viewModelScope.launch {
            val updatedFilters = _currentFilters.value.toMutableMap().apply {
                remove(filterKey)
            }.filterValues { it != null }
            _currentFilters.value = updatedFilters
            performSearch()
        }
    }

    fun removeAmenity(amenity: String) {
        viewModelScope.launch {
            val updatedFilters = _currentFilters.value.toMutableMap()
            val updatedAmenities = (updatedFilters["amenities"] as? List<String>)?.filter { it != amenity }
            if (updatedAmenities.isNullOrEmpty()) {
                updatedFilters.remove("amenities")
            } else {
                updatedFilters["amenities"] = updatedAmenities
            }
            _currentFilters.value = updatedFilters
            performSearch()
        }
    }

    private suspend fun performSearch() {
        val city = _currentFilters.value["city"] as? String
        val startDate = _currentFilters.value["startDate"] as? String
        val endDate = _currentFilters.value["endDate"] as? String
        val typeOfHome = _currentFilters.value["typeOfHome"] as? String
        val amenities = _currentFilters.value["amenities"] as? List<String>
        val rooms = _currentFilters.value["rooms"] as? Int
        val maxGuests = _currentFilters.value["maxGuests"] as? Int

        apartmentRepository.searchApartments(
            city = city,
            startDate = startDate,
            endDate = endDate,
            typeOfHome = typeOfHome,
            amenities = amenities,
            rooms = rooms,
            maxGuests = maxGuests
        )
    }

}
