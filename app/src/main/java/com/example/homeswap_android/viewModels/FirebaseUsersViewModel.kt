package com.example.homeswap_android.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FirebaseUsersViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    val usersCollectionReference = firestore.collection("users")
    private val userRepository = UserRepository(auth, storage, usersCollectionReference, firestore)

    val currentUser = userRepository.currentUser
    val currentUserData = userRepository.currentUserData
    val users = userRepository.users
    val loginResult = userRepository.loginResult
    val registerResult = userRepository.registerResult

    init {
        userRepository.setupUserEnv()
    }


    fun getApartmentDocumentReference(userID: String): DocumentReference {
        return userRepository.getUserDocumentReference(userID)
    }

    fun setProfile(profile: UserData) {
        userRepository.setProfile(profile)
    }

    fun login(email: String, password: String) {
        userRepository.login(email, password)
    }

    fun register(email: String, password: String, userData: UserData) {
        userRepository.register(email, password, userData)
    }

    fun sendEmailVerification(user: FirebaseUser) {
        userRepository.sendEmailVerification(user)
    }

    fun signOut() {
        userRepository.signOut()
    }

    fun fetchUsers() {
        userRepository.fetchUsers()
    }

    fun fetchUserData(userID: String) {
        userRepository.fetchUserData(userID)
    }

    fun uploadImage(uri: Uri) {
        userRepository.uploadImage(uri)
    }

    fun deleteUser() {
        userRepository.deleteUser()
    }

    fun checkEmailVerificationStatus(onComplete: (Boolean) -> Unit) {
        userRepository.checkEmailVerificationStatus(onComplete)
    }
}
