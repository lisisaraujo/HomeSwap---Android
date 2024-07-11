package com.example.homeswap_android.viewModels

import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.repositories.ApartmentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FiltersViewModel: ViewModel() {

    private val TAG = "FiltersViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val apartmentsCollectionReference = firestore.collection("apartments")
    private val apartmentRepository = ApartmentRepository(auth, storage, apartmentsCollectionReference)

}