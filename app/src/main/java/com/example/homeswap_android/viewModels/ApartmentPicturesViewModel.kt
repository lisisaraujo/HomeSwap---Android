package com.example.homeswap_android.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.repositories.ApartmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ApartmentPicturesViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val apartmentsCollectionReference = firestore.collection("apartments")
    private val apartmentRepository = ApartmentRepository(auth, storage, apartmentsCollectionReference)

    private val _currentPicture = MutableLiveData<String>()
    val currentPicture: LiveData<String> = _currentPicture

    private val _apartmentPictures = MutableLiveData<List<String>>()
    val apartmentPictures: LiveData<List<String>> = _apartmentPictures

    var picturePosition = 0

    fun loadApartmentPictures(apartmentID: String, userID: String) {
        apartmentRepository.getApartmentPictures(apartmentID, userID).observeForever { pictures ->
            _apartmentPictures.value = pictures
            if (pictures.isNotEmpty()) {
                updateCurrentPicture()
            }
        }
    }

    fun loadSinglePicture(position: Int) {
        picturePosition = position
        updateCurrentPicture()
    }

    fun loadNextPicture() {
        if (picturePosition < (_apartmentPictures.value?.size ?: 0) - 1) {
            picturePosition++
            updateCurrentPicture()
        }
    }

    fun loadPrevPicture() {
        if (picturePosition > 0) {
            picturePosition--
            updateCurrentPicture()
        }
    }

    private fun updateCurrentPicture() {
        _currentPicture.value = _apartmentPictures.value?.getOrNull(picturePosition)
    }
}
