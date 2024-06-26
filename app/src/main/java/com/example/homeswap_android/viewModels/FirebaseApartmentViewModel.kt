package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseApartmentViewModel : ViewModel() {
    val TAG = "FirebaseApartmentViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    val apartmentsCollectionReference = firestore.collection("apartments")
    var apartmentDataDocumentReference: DocumentReference? = null

    private val _apartments = MutableLiveData<List<Apartment>>()
    val apartments: LiveData<List<Apartment>>
        get() = _apartments

    private val _currentApartment = MutableLiveData<Apartment>()
    val currentApartment: LiveData<Apartment>
        get() = _currentApartment

    private val _likedApartments = MutableLiveData<List<Apartment>>()
    val likedApartments: LiveData<List<Apartment>>
        get() = _likedApartments

    private val _apartmentsBySearch = MutableLiveData<List<Apartment>>()
    val apartmentsBySearch: LiveData<List<Apartment>>
        get() = _apartmentsBySearch


    init {
        getApartments()
        loadLikedApartments()
    }

    fun getApartments() {
        apartmentsCollectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                val apartmentsList = querySnapshot.toObjects(Apartment::class.java)
                Log.d(TAG, "Fetched ${apartmentsList.size} apartments")
                apartmentsList.forEach { apartment ->
                    Log.d(TAG, "Apartment ${apartment.apartmentID}: liked=${apartment.liked}")
                }
                _apartments.postValue(apartmentsList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching apartments: $exception")
            }
    }

    fun getApartment(apartmentID: String) {
        apartmentsCollectionReference.document(apartmentID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val apartmentData = documentSnapshot.toObject(Apartment::class.java)
                    _currentApartment.postValue(apartmentData!!)
                } else {
                    Log.d(TAG, "Apartment data not found for ID: $apartmentID")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching apartment data: $exception")
            }
    }

    fun getUserApartments(userID: String): Query {
        return apartmentsCollectionReference.whereEqualTo("userID", userID)
    }

    fun addApartment(apartment: Apartment) {
        val currentUser = auth.currentUser ?: return
        val newApartment = apartment.copy(userID = currentUser.uid)

        apartmentsCollectionReference.add(newApartment)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Apartment added with ID: ${documentReference.id}")
                val updatedApartment = newApartment.copy(apartmentID = documentReference.id)
                documentReference.update("apartmentID", documentReference.id)
                _currentApartment.postValue(updatedApartment)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding apartment: $exception")
            }
    }


    fun uploadApartmentImage(uri: Uri, apartmentId: String) {
        val user = auth.currentUser
        if (user != null) {
            val imageRef = storage.reference.child("images/${user.uid}/apartments/$apartmentId")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        Log.d("ApartmentPicUrl", imageUrl)
                        updateApartmentPicture(apartmentId, imageUrl)
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting download URL: $exception")
                    }
                } else {
                    Log.e(TAG, "Upload failed: ${task.exception}")
                }
            }
        } else {
            Log.e(TAG, "User is not authenticated")
        }
    }

    private fun updateApartmentPicture(apartmentId: String, imageUrl: String) {
        apartmentsCollectionReference.document(apartmentId)
            .update("pictures", FieldValue.arrayUnion(imageUrl))
            .addOnSuccessListener {
                Log.d(TAG, "Apartment picture updated with URL: $imageUrl")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating apartment picture: $exception")
            }
    }

    fun deleteApartment(apartmentId: String) {
        viewModelScope.launch {
            apartmentsCollectionReference.document(apartmentId).delete()
                .addOnSuccessListener {
                    // refresh the user's apartments list
                    getUserApartments(auth.currentUser?.uid ?: return@addOnSuccessListener)
                }
        }
    }

    fun toggleLike(apartment: Apartment) {
        apartment.liked = !apartment.liked
        updateApartment(apartment)
    }

    fun updateApartment(apartment: Apartment) {
        viewModelScope.launch {
            apartmentsCollectionReference.document(apartment.apartmentID).set(apartment)
                .addOnSuccessListener {
                    getApartments()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating apartment ${apartment.apartmentID}: $e")
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
        val currentApt = currentApartment.value
        Log.d("ApartmentViewModel", "Current Apartment before update: $currentApt")

        val updatedApartment = currentApt?.copy(
            rooms = rooms,
            maxGuests = maxGuests,
            typeOfHome = typeOfHome,
            petsAllowed = petsAllowed,
            homeOffice = homeOffice,
            hasWifi = hasWifi
        )

        Log.d("ApartmentViewModel", "Updated Apartment: $updatedApartment")

        if (updatedApartment != null) {
            viewModelScope.launch {
                try {
                    updateApartment(updatedApartment)
                    _currentApartment.postValue(updatedApartment!!)
                    Log.d(TAG, "Apartment updated successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating apartment: ${e.message}")
                }
            }
        } else {
            Log.e(TAG, "Current apartment is null, can't update")
        }
    }

    fun loadLikedApartments() {
        apartmentsCollectionReference.whereEqualTo("liked", true).get()
            .addOnSuccessListener { querySnapshot ->
                val likedApartmentsList = querySnapshot.toObjects(Apartment::class.java)
                _likedApartments.postValue(likedApartmentsList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching liked apartments: $exception")
                _likedApartments.postValue(emptyList())
            }
    }

    fun clearSearch(){
        _apartmentsBySearch.postValue(listOf())
    }

    fun searchApartments(
        city: String? = null,
        country: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            val locationFilteredApartments = filterByLocation(city, country)
            val dateFilteredApartments =
                filterByDate(locationFilteredApartments, startDate, endDate)
            _apartmentsBySearch.postValue(dateFilteredApartments)
        }
    }

    private suspend fun filterByLocation(city: String?, country: String?): List<Apartment> =
        withContext(Dispatchers.IO) {
            if (city.isNullOrBlank() && country.isNullOrBlank()) {
                return@withContext apartments.value ?: emptyList()
            }

            var query: Query = apartmentsCollectionReference

            if (!city.isNullOrBlank()) {
                query = query.whereEqualTo("cityLower", city.lowercase())
            }

            if (!country.isNullOrBlank()) {
                query = query.whereEqualTo("countryLower", country.lowercase())
            }

            try {
                val querySnapshot = query.get().await()
                return@withContext querySnapshot.toObjects(Apartment::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching apartments by location: ${e.message}")
                return@withContext emptyList()
            }
        }

    private fun filterByDate(
        apartments: List<Apartment>,
        startDate: String?,
        endDate: String?
    ): List<Apartment> {
        if (startDate.isNullOrEmpty() || endDate.isNullOrEmpty()) {
            return apartments
        }
        return apartments.filter { apartment ->
            areDatesMatching(apartment.startDate, apartment.endDate, startDate, endDate)
        }
    }

    private fun areDatesMatching(
        apartmentStart: String,
        apartmentEnd: String,
        searchStart: String,
        searchEnd: String
    ): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // check if any of the date strings are empty
        if (apartmentStart.isBlank() || apartmentEnd.isBlank() || searchStart.isBlank() || searchEnd.isBlank()) {
            return false
        }
        return try {
            val dates = listOf(apartmentStart, apartmentEnd, searchStart, searchEnd)
                .map { dateFormat.parse(it) ?: throw ParseException("Invalid date format", 0) }

            !(dates[3].before(dates[0]) || dates[2].after(dates[1]))
        } catch (e: ParseException) {
            Log.e(TAG, "Error parsing date: ${e.message}")
            false
        }
    }
}
