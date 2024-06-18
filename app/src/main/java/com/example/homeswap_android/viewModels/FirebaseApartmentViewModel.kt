package com.example.homeswap_android.viewModels

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.util.copy
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Picture
import com.example.homeswap_android.data.models.UserData
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

    @SuppressLint("RestrictedApi")
    private fun setupApartmentEnv() {
        val user = auth.currentUser
        val apartment = _currentApartment.value
        _currentApartment.postValue(apartment!!)
        if (apartment != null) {
            apartmentDataDocumentReference = apartmentsCollectionReference.document(apartment.apartmentID)
            Log.d(TAG, apartment.title)

        }
    }

    fun setApartment(apartment: Apartment) {
        if (apartmentDataDocumentReference == null) {
            return
        }
        apartmentDataDocumentReference!!.set(apartment)
        Log.d("NewApartments", apartment.title)
    }

    fun addApartment(apartment: Apartment) {
        val currentUser = auth.currentUser ?: return

        val newApartment = apartment.copy(userID = currentUser.uid)

        apartmentsCollectionReference.add(newApartment)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "FirebaseApartmentViewModel",
                    "Apartment added with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseApartmentViewModel", "Error adding apartment: $exception")
            }
        _currentApartment.value = newApartment
        Log.d(TAG, currentApartment.value.toString())
    }

    fun uploadImage(uri: Uri, apartmentId: String) {
        val currentUser = auth.currentUser ?: return
        val imageRef = storage.reference.child("images/apartments/$apartmentId/${uri.lastPathSegment}")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()
                    Log.d("ApartmentImageUrl", imageUrl)

                    // Update the apartment document with the new picture URL
                    val newPicture = Picture(imageUrl)
                    firestore.collection("apartments").document(apartmentId)
                        .update("pictures", FieldValue.arrayUnion(newPicture))
                        .addOnSuccessListener {
                            Log.d("FirebaseApartmentViewModel", "Picture added to apartment $apartmentId")
                        }
                        .addOnFailureListener { exception ->
                            Log.e("FirebaseApartmentViewModel", "Error adding picture: $exception")
                        }
                }
            } else {
                Log.e("FirebaseApartmentViewModel", "Upload failed: ${task.exception}")
            }
        }
    }

}
