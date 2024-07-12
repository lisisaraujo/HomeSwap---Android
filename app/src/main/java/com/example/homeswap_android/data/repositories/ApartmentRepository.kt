package com.example.homeswap_android.data.repositories

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.utils.Utils.dateFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.tasks.await
import java.text.ParseException

val TAG = "ApartmentRepository"

class ApartmentRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val apartmentsCollectionReference: CollectionReference,
) {

    private val _apartments = MutableLiveData<List<Apartment>>()
    val apartments: LiveData<List<Apartment>> = _apartments

    private val _loadingApartments = MutableLiveData<Boolean>()
    val loadingApartments: LiveData<Boolean> = _loadingApartments

    private val _currentApartment = MutableLiveData<Apartment?>()
    val currentApartment: LiveData<Apartment?> = _currentApartment

    private val _newAddedApartment = MutableLiveData<Apartment?>()
    val newAddedApartment: MutableLiveData<Apartment?> = _newAddedApartment

    private val _likedApartments = MutableLiveData<List<Apartment>>()
    val likedApartments: LiveData<List<Apartment>> = _likedApartments

    private val _apartmentsBySearch = MutableLiveData<List<Apartment>>()
    val apartmentsBySearch: LiveData<List<Apartment>> = _apartmentsBySearch

    private val _searchCompletedEvent = MutableSharedFlow<Unit>()
    val searchCompletedEvent: SharedFlow<Unit> = _searchCompletedEvent


    init {
        apartmentsCollectionReference.addSnapshotListener { value, error ->
            _apartments.postValue(value!!.toObjects(Apartment::class.java))

        }
    }

    fun getApartmentDocumentReference(apartmentId: String): DocumentReference {
        return apartmentsCollectionReference.document(apartmentId)
    }

    fun getApartments() {
        apartmentsCollectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                val apartmentsList = querySnapshot.toObjects(Apartment::class.java)
                _apartments.postValue(apartmentsList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching apartments: ${exception.message}")
                _apartments.postValue(emptyList())
            }
    }

    fun getApartment(apartmentID: String): LiveData<Apartment> {
        var resultLiveData: MutableLiveData<Apartment> = MutableLiveData()
        apartmentsCollectionReference.document(apartmentID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val apartment = documentSnapshot.toObject(Apartment::class.java)
                    resultLiveData.postValue(apartment)
                } else {
                    Log.d(TAG, "Apartment document does not exist for ID: $apartmentID")
                    resultLiveData.postValue(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching apartment: ${exception.message}")
                resultLiveData.postValue(null)
            }
        return resultLiveData
    }

    fun getUserApartments(userID: String): Query {
        return apartmentsCollectionReference.whereEqualTo("userID", userID)
    }

    fun getApartmentPictures(apartmentID: String, userID: String): LiveData<List<String>> {
        val picturesLiveData = MutableLiveData<List<String>>()
        val apartmentPicturesRef = storage.reference.child("images/$userID/apartments/$apartmentID")

        apartmentPicturesRef.listAll()
            .addOnSuccessListener { (items, _) ->
                val imageUrls = mutableListOf<String>()
                items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        imageUrls.add(uri.toString())
                        picturesLiveData.postValue(imageUrls)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error listing images: ${exception.message}")
                picturesLiveData.postValue(emptyList())
            }

        return picturesLiveData
    }

    fun uploadApartmentImages(
        uris: List<Uri>,
        apartmentID: String,
        onComplete: (List<String>) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            Log.e(TAG, "No user logged in")
            onComplete(emptyList())
            return
        }

        uris.mapIndexed { index, uri ->
            val imageRef =
                storage.reference.child("images/${user.uid}/apartments/$apartmentID/image_$index")
            imageRef.putFile(uri).addOnSuccessListener {
                if (index == 0) {
                    imageRef.downloadUrl.addOnSuccessListener { url ->
                        apartmentsCollectionReference.document(apartmentID)
                            .update("coverPicture", url.toString())
                    }
                }
            }
        }
    }


    fun addApartment(apartment: Apartment, onComplete: (Apartment?) -> Unit) {
        val currentUser = auth.currentUser
        currentUser.let {
            val newApartment = apartment.copy(userID = currentUser!!.uid)
            apartmentsCollectionReference.add(newApartment)
                .addOnSuccessListener { documentReference ->
                    val updatedApartment = newApartment.copy(apartmentID = documentReference.id)
                    documentReference.update("apartmentID", documentReference.id)
                        .addOnSuccessListener {
                            _newAddedApartment.postValue(updatedApartment)
                            onComplete(updatedApartment)
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error updating apartment ID: ${exception.message}")
                            onComplete(null)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error adding apartment: ${exception.message}")
                    onComplete(null)
                }
        }

    }


    fun updateApartmentImageURLs(
        apartmentID: String,
        imageUrls: List<String>,
        onComplete: (Boolean) -> Unit
    ) {
        apartmentsCollectionReference.document(apartmentID)
            .update("pictures", FieldValue.arrayUnion(*imageUrls.toTypedArray()))
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating apartment pictures: ${exception.message}")
                onComplete(false)
            }
    }


    private fun deleteApartmentPictures(
        apartmentID: String,
        userID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val apartmentPicturesRef = storage.reference.child("images/$userID/apartments/$apartmentID")

        apartmentPicturesRef.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isEmpty()) {
                    // no pictures to delete
                    onSuccess()
                    return@addOnSuccessListener
                }

                var deletedCount = 0
                var hasError = false

                listResult.items.forEach { item ->
                    item.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount == listResult.items.size && !hasError) {
                                Log.d(TAG, "All apartment pictures deleted successfully")
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { exception ->
                            if (!hasError) {
                                hasError = true
                                Log.e(TAG, "Error deleting apartment picture: ${exception.message}")
                                onFailure(exception)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error listing apartment pictures: ${exception.message}")
                onFailure(exception)
            }
    }


    fun deleteApartment(
        apartmentID: String,
        userID: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        deleteApartmentPictures(
            apartmentID,
            userID,
            onSuccess = {
                //pictures deleted successfully, now delete the apartment
                apartmentsCollectionReference.document(apartmentID).delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Apartment with ID $apartmentID deleted successfully")
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error deleting apartment document: ${exception.message}")
                        onFailure(exception)
                    }
            },
            onFailure = { exception ->
                Log.w(
                    TAG,
                    "Failed to delete pictures for apartment $apartmentID: ${exception.message}"
                )
                //proceed with deleting the apartment document even if picture deletion fails
                apartmentsCollectionReference.document(apartmentID).delete()
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "Apartment with ID $apartmentID deleted successfully, but picture deletion failed"
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { docException ->
                        Log.e(TAG, "Error deleting apartment document: ${docException.message}")
                        onFailure(docException)
                    }
            }
        )
    }


    fun updateApartment(apartment: Apartment) {
        apartmentsCollectionReference.document(apartment.apartmentID).set(apartment)
            .addOnSuccessListener {
                Log.d(TAG, "Apartment updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating apartment: ${exception.message}")
            }
    }

    fun getLikedApartments() {
        apartmentsCollectionReference.whereEqualTo("liked", true).get()
            .addOnSuccessListener { querySnapshot ->
                val likedApartments = querySnapshot.toObjects(Apartment::class.java)
                _likedApartments.postValue(likedApartments)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading liked apartments: ${exception.message}")
                _likedApartments.postValue(emptyList())
            }
    }

    fun clearSearch() {
        _apartmentsBySearch.postValue(emptyList())
    }

    fun resetNewAddedApartment() {
        _newAddedApartment.postValue(null)
    }

    suspend fun searchApartments(
        city: String?,
        startDate: String?,
        endDate: String?,
        typeOfHome: String?,
        amenities: List<String>?,
        rooms: Int?,
        maxGuests: Int?
    ) {
        var query: Query = apartmentsCollectionReference

        if (!city.isNullOrBlank()) {
            query = query.whereEqualTo("cityLower", city.lowercase())
        }

        if (!typeOfHome.isNullOrBlank()) {
            query = query.whereEqualTo("typeOfHome", typeOfHome)
        }

        //apply amenity filters
        if (amenities != null) {
            if (amenities.contains("Pets Allowed")) {
                query = query.whereEqualTo("petsAllowed", true)
            }

            if (amenities.contains("Home Office")) {
                query = query.whereEqualTo("homeOffice", true)
            }
            if (amenities.contains("Has Wifi")) {
                query = query.whereEqualTo("hasWifi", true)
            }
        }

        if (rooms != null && rooms > 0) {
            query = query.whereGreaterThanOrEqualTo("rooms", rooms)
        }

        if (maxGuests != null && maxGuests > 0) {
            query = query.whereGreaterThanOrEqualTo("maxGuests", maxGuests)
        }

        query.get()

        _loadingApartments.postValue(true)

        try {
            val querySnapshot = query.get().await()
            val apartments = querySnapshot.toObjects(Apartment::class.java)
            Log.d(TAG, "Apartments fetched: ${apartments.size}")
            val filteredApartments = filterByDate(apartments, startDate, endDate)
            Log.d(TAG, "Filtered Apartments: ${filteredApartments.size}")
            _apartmentsBySearch.value = filteredApartments
        } catch (exception: Exception) {
            Log.e(TAG, "Error searching apartments: ${exception.message}")
            _apartmentsBySearch.value = emptyList()
        } finally {
            _loadingApartments.value = false
            _searchCompletedEvent.emit(Unit)
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
        if (apartmentStart.isBlank() || apartmentEnd.isBlank() || searchStart.isBlank() || searchEnd.isBlank()) {
            return false
        }

        return try {
            val dates = listOf(apartmentStart, apartmentEnd, searchStart, searchEnd)
                .map { dateFormat.parse(it) ?: throw ParseException("Invalid date format", 0) }

            !(dates[3].before(dates[0]) || dates[2].after(dates[1]))
        } catch (e: ParseException) {
            false
        }
    }

}