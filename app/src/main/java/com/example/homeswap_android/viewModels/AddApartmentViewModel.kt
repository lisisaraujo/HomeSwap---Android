package com.example.homeswap_android.viewModels

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

    fun addApartment(apartment: Apartment) {
        _isLoading.value = true
        apartmentRepository.addApartment(apartment) { updatedApartment ->
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
        newAddedApartment.value?.let { apartment ->
            val updatedApartment = apartment.copy(
                rooms = rooms,
                maxGuests = maxGuests,
                typeOfHome = typeOfHome,
                petsAllowed = petsAllowed,
                homeOffice = homeOffice,
                hasWifi = hasWifi
            )

            apartmentRepository.updateApartment(updatedApartment)
            resetNewAddedApartment()
        } ?: Log.e(TAG, "No apartment to update")
    }

    private fun resetNewAddedApartment() {
        apartmentRepository.resetNewAddedApartment()
    }
}
