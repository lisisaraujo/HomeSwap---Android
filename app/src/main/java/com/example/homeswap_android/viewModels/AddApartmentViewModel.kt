package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.repositories.ApartmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddApartmentViewModel : ViewModel() {

    private val TAG = "AddApartmentViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val apartmentsCollectionReference = firestore.collection("apartments")
    private val apartmentRepository = ApartmentRepository(auth, storage, apartmentsCollectionReference)

    val newAddedApartment = apartmentRepository.newAddedApartment
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _tempApartmentDetails = MutableLiveData<Apartment>()
    val tempApartmentDetails: LiveData<Apartment> get() = _tempApartmentDetails

    private val _tempImageUris = MutableLiveData<List<Uri>>()
    val tempImageUris: LiveData<List<Uri>> get() = _tempImageUris

    fun setTempApartmentDetails(apartment: Apartment) {
        _tempApartmentDetails.value = apartment
    }

    fun setTempImageUris(uris: List<Uri>) {
        _tempImageUris.value = uris
    }

    private fun addApartment(apartment: Apartment, imageUris: List<Uri>) {
        _isLoading.value = true
        apartmentRepository.addApartment(apartment, imageUris) { updatedApartment ->
            if (updatedApartment != null) {
                _isLoading.postValue(false)
                apartmentRepository.getApartments()
            } else {
                _isLoading.postValue(false)
                Log.e(TAG, "Error adding apartment")
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
        _tempApartmentDetails.value?.let { apartment ->
            val updatedApartment = apartment.copy(
                rooms = rooms,
                maxGuests = maxGuests,
                typeOfHome = typeOfHome,
                petsAllowed = petsAllowed,
                homeOffice = homeOffice,
                hasWifi = hasWifi
            )
            addApartment(updatedApartment, _tempImageUris.value ?: emptyList())
        } ?: Log.e(TAG, "No apartment to update")
    }

    private fun resetNewAddedApartment() {
        apartmentRepository.resetNewAddedApartment()
    }
}
