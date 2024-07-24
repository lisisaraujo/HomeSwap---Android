package com.example.homeswap_android.data.repositories

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val usersCollectionReference: CollectionReference,
    private val firestore: FirebaseFirestore,
) {

    val TAG = "userRepository"

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private val _loggedInUser = MutableLiveData<FirebaseUser?>()
    val loggedInUser: LiveData<FirebaseUser?>
        get() = _loggedInUser

    private val _loggedInUserData = MutableStateFlow<UserData?>(null)
    val loggedInUserData: StateFlow<UserData?>
        get() = _loggedInUserData


    private val _selectedUserData = MutableStateFlow<UserData?>(null)
    val selectedUserData: StateFlow<UserData?>
        get() = _selectedUserData

    private val _users = MutableLiveData<List<UserData>>()
    val users: LiveData<List<UserData>>
        get() = _users

    private val _loginResult = MutableSharedFlow<Boolean>()
    val loginResult: SharedFlow<Boolean> get() = _loginResult

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean>
        get() = _registerResult

    private var userDataDocumentReference = MutableLiveData<DocumentReference?>()

    init {
        usersCollectionReference.addSnapshotListener { value, error ->
            Log.d(TAG, "value: ${value.toString()}")
            val usersObject = value!!.toObjects(UserData::class.java)
            Log.d(TAG, usersObject.toString())
            _users.postValue(usersObject)
        }

        auth.currentUser?.let { setupUserEnv() }
        refreshLoggedInUserData()

    }

    fun getUserDocumentReference(userID: String): DocumentReference {
        return usersCollectionReference.document(userID)
    }


    fun setupUserEnv() {
        val user = auth.currentUser

        if (user != null) {
            _loggedInUser.postValue(user)
            userDataDocumentReference.postValue(usersCollectionReference.document(user.uid))
            Log.d("NewUser", user.email.toString())
            getLoggedInUserData(user.uid)
        }
    }


    private fun getLoggedInUserData(userID: String) {
        usersCollectionReference.document(userID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(UserData::class.java)
                    _loggedInUserData.value = userData
                } else {
                    Log.d(TAG, "User data not found for ID: $userID")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user data: $exception")
            }
    }

    fun setProfile(profile: UserData) {
        userDataDocumentReference.value?.set(profile)
        userDataDocumentReference.value?.update("userID", auth.currentUser?.uid)
        _loggedInUserData.value = profile
    }

    suspend fun login(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            setupUserEnv()
            _loginResult.emit(true)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed: ${e.message}")
            _loginResult.emit(false)
        }
        refreshLoggedInUserData()
    }


    fun register(email: String, password: String, userData: UserData) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                setupUserEnv()
                val user = authResult.user!!
                Log.d(TAG, user.uid)
                sendEmailVerification(user)

                val userDataWithID = userData.copy(userID = user.uid)

                usersCollectionReference.document(user.uid).set(userDataWithID)
                    .addOnSuccessListener {
                        Log.d("FirebaseViewModel", "New user profile created successfully")
                        _registerResult.value = true
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "FirebaseViewModel",
                            "Error creating new user profile: $exception"
                        )
                        _registerResult.value = false
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error creating user: $exception")
                _registerResult.value = false
            }
    }

    fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                } else {
                    Log.d(TAG, "onFailure: Email not sent" + task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _loggedInUser.postValue(null)
        Log.d(TAG, loggedInUser.value?.email ?: "No user logged in")

    }

    suspend fun fetchUsers(): List<UserData> {
        return try {
            val querySnapshot = usersCollectionReference.get().await()
            querySnapshot.toObjects(UserData::class.java)
        } catch (exception: Exception) {
            Log.e("FirebaseViewModel", "Error fetching users: $exception")
            emptyList()
        }
    }

    fun fetchSelectedUserData(userID: String) {
        usersCollectionReference.document(userID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(UserData::class.java)
                    _selectedUserData.value = userData
                } else {
                    Log.d(TAG, "User data not found for ID: $userID")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user data: $exception")
            }
    }



    fun uploadImage(uri: Uri) {
        val user = auth.currentUser
        if (user != null) {
            val imageRef = storage.reference.child("images/${user.uid}/profilePic")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnSuccessListener {
                        val imageUrl = it.toString()
                        Log.d("ProfilePicUrl", imageUrl)
                        userDataDocumentReference.value?.update("profilePic", imageUrl)
                    }.addOnFailureListener { exception ->
                        Log.e("FirebaseViewModel", "Error getting download URL: $exception")
                    }
                } else {
                    Log.e("FirebaseViewModel", "Upload failed: ${task.exception}")
                }
            }
        } else {
            Log.e("FirebaseViewModel", "User is not authenticated")
        }
    }

    fun deleteUser(password: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            onFailure("No user is currently logged in")
            return
        }
        val userId = currentUser.uid

        //re-authenticate user before deletion
        reAuthenticateUser(currentUser, password, {
            deleteUserData(userId,
                onSuccess = {
                    deleteAuthUser(currentUser, onSuccess, onFailure)
                },
                onFailure = { error ->
                    onFailure("Failed to delete user data: $error")
                }
            )
        }, onFailure)
    }

    private fun reAuthenticateUser(
        user: FirebaseUser,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure("Re-authentication failed: ${exception.message}")
            }
    }

    private fun deleteUserData(userId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        deleteUserApartments(userId, {
            deleteUserReviews(userId, {
                deleteUserProfilePicture(userId, {
                    deleteUserDocument(userId, onSuccess, onFailure)
                }, onFailure)
            }, onFailure)
        }, onFailure)
    }

    private fun deleteUserApartments(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("apartments").whereEqualTo("userID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                var deletedCount = 0
                val totalApartments = querySnapshot.size()

                for (document in querySnapshot.documents) {
                    val apartment = document.toObject(Apartment::class.java)
                    apartment?.let {
                        deleteApartmentAndRelatedData(it.apartmentID, userId, {
                            deletedCount++
                            if (deletedCount == totalApartments) {
                                onSuccess()
                            }
                        }, onFailure)
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error fetching user's apartments: $exception")
            }
    }

    private fun deleteApartmentAndRelatedData(
        apartmentID: String,
        userID: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        deleteApartmentPictures(apartmentID, userID, {
            deleteApartmentReviews(apartmentID, {
                deleteApartmentDocument(apartmentID, onSuccess, onFailure)
            }, onFailure)
        }, onFailure)
    }

    private fun deleteApartmentPictures(
        apartmentID: String,
        userID: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val apartmentPicturesRef = storage.reference.child("images/$userID/apartments/$apartmentID")
        apartmentPicturesRef.listAll()
            .addOnSuccessListener { listResult ->
                if (listResult.items.isEmpty()) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                var deletedCount = 0
                val totalItems = listResult.items.size

                for (item in listResult.items) {
                    item.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount == totalItems) {
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure("Error deleting apartment picture: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error listing apartment pictures: $exception")
            }
    }

    private fun deleteApartmentReviews(
        apartmentID: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("reviews").whereEqualTo("destinationID", apartmentID).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                var deletedCount = 0
                val totalReviews = querySnapshot.size()

                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount == totalReviews) {
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure("Error deleting apartment review: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error fetching apartment reviews: $exception")
            }
    }

    private fun deleteUserReviews(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("reviews").whereEqualTo("destinationID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                var deletedCount = 0
                val totalReviews = querySnapshot.size()

                for (document in querySnapshot.documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount == totalReviews) {
                                onSuccess()
                            }
                        }
                        .addOnFailureListener { exception ->
                            onFailure("Error deleting user review: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                onFailure("Error fetching user reviews: $exception")
            }
    }

    private fun deleteUserProfilePicture(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val profilePicRef = storage.reference.child("images/$userId/profilePic")
        profilePicRef.metadata
            .addOnSuccessListener {
                // If metadata exists, delete the file
                profilePicRef.delete()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onFailure("Error deleting profile picture: $exception")
                    }
            }
            .addOnFailureListener { exception ->
                if ((exception as StorageException).errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    // Profile picture does not exist, proceed with the deletion process
                    onSuccess()
                } else {
                    onFailure("Error checking profile picture existence: $exception")
                }
            }
    }

    private fun deleteUserDocument(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        usersCollectionReference.document(userId).delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure("Error deleting user document: $exception")
            }
    }

    private fun deleteApartmentDocument(
        apartmentID: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("apartments").document(apartmentID).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Apartment deleted successfully: $apartmentID")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting apartment: $exception")
                onFailure("Error deleting apartment: $exception")
            }
    }

    private fun deleteAuthUser(
        currentUser: FirebaseUser,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        currentUser.delete()
            .addOnSuccessListener {
                Log.d(TAG, "User deleted successfully from Authentication")
                signOut()
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting user from Authentication: $exception")
                onFailure("Error deleting user from Authentication: $exception")
            }
    }

    fun checkEmailVerificationStatus(onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(user.isEmailVerified)
            } else {
                Log.e("FirebaseUsersViewModel", "Failed to reload user", task.exception)
                onComplete(false)
            }
        }
    }

    suspend fun updateUserData(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
            refreshLoggedInUserData()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user data", e)
            false
        }
    }


    fun refreshLoggedInUserData() {
        Log.d(TAG, "Refreshing logged-in user data")
        auth.currentUser?.let { user ->
            Log.d(TAG, "Current user UID: ${user.uid}")
            getLoggedInUserData(user.uid)
        } ?: Log.d(TAG, "No current user")
    }

    fun updateProfilePicture(uri: Uri, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val imageRef = storage.reference.child("images/${user.uid}/profilePic")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        Log.d(TAG, "ProfilePicUrl: $imageUrl")
                        usersCollectionReference.document(user.uid).update("profilePic", imageUrl)
                            .addOnSuccessListener {
                                _loggedInUserData.value?.profilePic = imageUrl
                                onComplete(true)
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error updating profile picture: ${exception.message}")
                                onComplete(false)
                            }
                    }.addOnFailureListener { exception ->
                        Log.e(TAG, "Error getting download URL: ${exception.message}")
                        onComplete(false)
                    }
                } else {
                    Log.e(TAG, "Upload failed: ${task.exception}")
                    onComplete(false)
                }
            }
        } else {
            Log.e(TAG, "User is not authenticated")
            onComplete(false)
        }
    }


}