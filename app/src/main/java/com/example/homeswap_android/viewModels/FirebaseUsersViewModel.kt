package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FirebaseUsersViewModel : ViewModel() {

    val TAG = "FirebaseUsersViewModel"

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    val usersCollectionReference = firestore.collection("users")
    private val userRepository = UserRepository(auth, storage, usersCollectionReference, firestore)

    val loggedInUser = userRepository.loggedInUser
    val loggedInUserData = userRepository.loggedInUserData
    val users = userRepository.users
    val loginResult = userRepository.loginResult
    val registerResult = userRepository.registerResult
    val selectedUserData = userRepository.selectedUserData


    val currentUser = userRepository.currentUser

    init {
        userRepository.setupUserEnv()
        refreshLoggedInUserData()
    }


    fun getUserDocumentReference(userID: String): DocumentReference {
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
        viewModelScope.launch {
            try {
                userRepository.fetchUsers()
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching users", e)
            }
        }
    }

    fun fetchSelectedUserData(userID: String) {
        userRepository.fetchSelectedUserData(userID)
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

    fun updateUserData(userId: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = userRepository.updateUserData(userId, updates)
            if (success) {
                refreshLoggedInUserData()
            }
            onComplete(success)
        }
    }

    fun updateProfilePicture(uri: Uri, onComplete: (Boolean) -> Unit) {
        userRepository.updateProfilePicture(uri, onComplete)
    }


    fun refreshLoggedInUserData() {
        userRepository.refreshLoggedInUserData()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
