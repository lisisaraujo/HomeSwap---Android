package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.Apartment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

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
    private val _userApartments = MutableLiveData<List<Apartment>>()
    val userApartments: LiveData<List<Apartment>>
        get() = _userApartments

    private val _likedApartments = MutableLiveData<List<Apartment>>()
    val likedApartments: LiveData<List<Apartment>>
        get() = _likedApartments

    private val _apartmentsByCity = MutableLiveData<List<Apartment>>()
    val apartmentsByLocation: LiveData<List<Apartment>>
        get() = _apartmentsByCity


    init {
        fetchApartments()
        loadLikedApartments()
    }


    fun fetchApartments() {
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

    fun fetchApartment(apartmentID: String) {
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

    fun fetchUserApartments(userID: String): Query {
        return apartmentsCollectionReference.whereEqualTo("userID", userID)
    }

    fun addApartment(apartment: Apartment) {
        val currentUser = auth.currentUser ?: return

        val newApartment = apartment.copy(userID = currentUser.uid)

        apartmentsCollectionReference.add(newApartment)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Apartment added with ID: ${documentReference.id}")
                _currentApartment.postValue(newApartment.copy(apartmentID = documentReference.id))
                documentReference.update("apartmentID", documentReference.id)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error adding apartment: $exception")
            }
        fetchApartments()
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

    fun updateApartmentPicture(apartmentId: String, imageUrl: String) {
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
                    fetchUserApartments(auth.currentUser?.uid ?: return@addOnSuccessListener)
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
                    fetchApartments()
//                    auth.currentUser?.uid?.let { userId -> fetchUserApartments(userId) }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error updating apartment ${apartment.apartmentID}: $e")
                }
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

    fun searchByCity(city: String){
        apartmentsCollectionReference.whereEqualTo ("city", city).get()
            .addOnSuccessListener { querySnapshot ->
                val apartmentsByCityList = querySnapshot.toObjects(Apartment::class.java)
                _apartmentsByCity.postValue(apartmentsByCityList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching liked apartments by city: $exception")
                _apartmentsByCity.postValue(emptyList())
            }
    }
}
