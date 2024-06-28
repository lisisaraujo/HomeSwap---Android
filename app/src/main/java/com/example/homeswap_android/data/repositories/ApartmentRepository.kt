package com.example.homeswap_android.data.repositories

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Apartment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.component1
import com.google.firebase.storage.component2
import kotlinx.coroutines.tasks.await
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class ApartmentRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val apartmentsCollectionReference: CollectionReference,
) {

    private val _apartments = MutableLiveData<List<Apartment>>()
    val apartments: LiveData<List<Apartment>> = _apartments

    private val _currentApartment = MutableLiveData<Apartment>()
    val currentApartment: LiveData<Apartment> = _currentApartment

    private val _likedApartments = MutableLiveData<List<Apartment>>()
    val likedApartments: LiveData<List<Apartment>> = _likedApartments

    private val _apartmentsBySearch = MutableLiveData<List<Apartment>>()
    val apartmentsBySearch: LiveData<List<Apartment>> = _apartmentsBySearch


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

    fun getApartment(apartmentID: String) {
        apartmentsCollectionReference.document(apartmentID).get()
            .addOnSuccessListener { documentSnapshot ->
                val apartment = documentSnapshot.toObject(Apartment::class.java)
                _currentApartment.postValue(apartment)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching apartment: ${exception.message}")
            }
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

    fun addApartment(apartment: Apartment, onComplete: (Apartment?) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            onComplete(null)
            return
        }
        val newApartment = apartment.copy(userID = currentUser.uid)
        apartmentsCollectionReference.add(newApartment)
            .addOnSuccessListener { documentReference ->
                val updatedApartment = newApartment.copy(apartmentID = documentReference.id)
                documentReference.update("apartmentID", documentReference.id)
                    .addOnSuccessListener {
                        _currentApartment.postValue(updatedApartment)
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

    fun uploadApartmentImage(uri: Uri, apartmentID: String, onComplete: (String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(null)
            return
        }
        val imageRef = storage.reference.child("images/${user.uid}/apartments/$apartmentID/image")
        imageRef.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    onComplete(downloadUri.toString())
                } else {
                    Log.e(TAG, "Error uploading image: ${task.exception?.message}")
                    onComplete(null)
                }
            }
    }

    fun updateApartmentPicture(apartmentID: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        apartmentsCollectionReference.document(apartmentID)
            .update("pictures", FieldValue.arrayUnion(imageUrl))
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating apartment picture: ${exception.message}")
                onComplete(false)
            }
    }

    fun deleteApartment(apartmentID: String, onComplete: (Boolean) -> Unit) {
        apartmentsCollectionReference.document(apartmentID).delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting apartment: ${exception.message}")
                onComplete(false)
            }
    }

    fun updateApartment(apartment: Apartment, onComplete: (Boolean) -> Unit) {
        apartmentsCollectionReference.document(apartment.apartmentID).set(apartment)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error updating apartment: ${exception.message}")
                onComplete(false)
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

    fun searchApartments(
        city: String?,
        country: String?,
        startDate: String?,
        endDate: String?
    ) {
        var query: Query = apartmentsCollectionReference

        if (!city.isNullOrBlank()) {
            query = query.whereEqualTo("cityLower", city.lowercase())
        }

        if (!country.isNullOrBlank()) {
            query = query.whereEqualTo("countryLower", country.lowercase())
        }

        query.get()
            .addOnSuccessListener { querySnapshot ->
                val apartments = querySnapshot.toObjects(Apartment::class.java)
                val filteredApartments = filterByDate(apartments, startDate, endDate)
                _apartmentsBySearch.postValue(filteredApartments)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error searching apartments: ${exception.message}")
                _apartmentsBySearch.postValue(emptyList())
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