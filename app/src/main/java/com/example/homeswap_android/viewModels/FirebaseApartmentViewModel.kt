package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.Apartment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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

    init {
        fetchApartments()
    }

    fun fetchApartments() {
        apartmentsCollectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                val apartmentsList = querySnapshot.toObjects(Apartment::class.java)
                _apartments.postValue(apartmentsList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching apartments: $exception")
            }
    }

    fun addApartment(apartment: Apartment) {
        val currentUser = auth.currentUser ?: return

        val newApartment = apartment.copy(userID = currentUser.uid)

        apartmentsCollectionReference.add(newApartment)
            .addOnSuccessListener { documentReference ->
                Log.d("FirebaseApartmentViewModel", "Apartment added with ID: ${documentReference.id}")
                _currentApartment.postValue(newApartment.copy(apartmentID = documentReference.id))
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseApartmentViewModel", "Error adding apartment: $exception")
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
}
