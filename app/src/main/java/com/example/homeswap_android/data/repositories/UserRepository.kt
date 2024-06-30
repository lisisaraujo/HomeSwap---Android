package com.example.homeswap_android.data.repositories

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserRepository(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val usersCollectionReference: CollectionReference,
    private val firestore: FirebaseFirestore,
) {

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private val _currentUserData = MutableLiveData<UserData?>()
    val currentUserData: LiveData<UserData?>
        get() = _currentUserData

    private val _users = MutableLiveData<List<UserData>>()
    val users: LiveData<List<UserData>>
        get() = _users

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean>
        get() = _loginResult

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean>
        get() = _registerResult

    private var userDataDocumentReference = MutableLiveData<DocumentReference?>()

    init {
        usersCollectionReference.addSnapshotListener { value, error ->
            _users.postValue(value!!.toObjects(UserData::class.java))

        }
    }

    fun getUserDocumentReference(userID: String): DocumentReference {
        return usersCollectionReference.document(userID)
    }


    fun setupUserEnv() {
        val user = auth.currentUser
        _currentUser.postValue(user)
        if (user != null) {
            userDataDocumentReference.postValue(usersCollectionReference.document(user.uid))
            Log.d("NewUser", user.email.toString())
        }
    }

    fun setProfile(profile: UserData) {
        userDataDocumentReference.value?.set(profile)
        userDataDocumentReference.value?.update("userID", userDataDocumentReference.value?.id)
        Log.d("NewProfile", profile.userID)
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setupUserEnv()
                _loginResult.value = true
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Login failed: ${exception.message}")
                _loginResult.value = false
            }
    }

    fun register(email: String, password: String, userData: UserData) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                setupUserEnv()
                val user = authResult.user!!
                sendEmailVerification(user)

                usersCollectionReference.document(user.uid).set(userData)
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
        setupUserEnv()
    }

    fun fetchUsers() {
        usersCollectionReference.get()
            .addOnSuccessListener { querySnapshot ->
                val usersList = querySnapshot.toObjects(UserData::class.java)
                _users.postValue(usersList)
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseViewModel", "Error fetching users: $exception")
            }
    }

    fun fetchUserData(userID: String) {
        usersCollectionReference.document(userID).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.toObject(UserData::class.java)
                    _currentUserData.postValue(userData)
                } else {
                    Log.d("FirebaseUsersViewModel", "User data not found for ID: $userID")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseUsersViewModel", "Error fetching user data: $exception")
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

    fun deleteUser() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        deleteUserApartments(userId)
    }

    private fun deleteUserApartments(userId: String) {
        firestore.collection("apartments").whereEqualTo("userID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                var deletedCount = 0
                val totalApartments = querySnapshot.size()

                if (totalApartments == 0) {
                    deleteUserProfilePicture(userId)
                    return@addOnSuccessListener
                }

                for (document in querySnapshot.documents) {
                    val apartment = document.toObject(Apartment::class.java)
                    apartment?.let {
                        deleteApartmentAndPictures(it.apartmentID, userId) {
                            deletedCount++
                            if (deletedCount == totalApartments) {
                                deleteUserProfilePicture(userId)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user's apartments: $exception")
            }
    }

    private fun deleteApartmentAndPictures(apartmentID: String, userID: String, onAllDeleted: () -> Unit) {
        val apartmentPicturesRef = storage.reference.child("images/$userID/apartments/$apartmentID")

        apartmentPicturesRef.listAll()
            .addOnSuccessListener { listResult ->
                var deletedCount = 0
                val totalItems = listResult.items.size

                if (totalItems == 0) {
                    deleteApartmentDocument(apartmentID, onAllDeleted)
                    return@addOnSuccessListener
                }

                for (item in listResult.items) {
                    item.delete()
                        .addOnSuccessListener {
                            deletedCount++
                            if (deletedCount == totalItems) {
                                deleteApartmentDocument(apartmentID, onAllDeleted)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "Error deleting apartment picture: $exception")
                            deletedCount++
                            if (deletedCount == totalItems) {
                                deleteApartmentDocument(apartmentID, onAllDeleted)
                            }
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error listing apartment pictures: $exception")
                deleteApartmentDocument(apartmentID, onAllDeleted)
            }
    }

    private fun deleteApartmentDocument(apartmentID: String, onDeleted: () -> Unit) {
        firestore.collection("apartments").document(apartmentID).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Apartment deleted successfully: $apartmentID")
                onDeleted()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting apartment: $exception")
                onDeleted()
            }
    }

    private fun deleteUserProfilePicture(userId: String) {
        val profilePicRef = storage.reference.child("images/$userId/profilePic")
        profilePicRef.delete()
            .addOnSuccessListener {
                Log.d(TAG, "Profile picture deleted successfully")
                deleteUserDocument(userId)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting profile picture: $exception")
                deleteUserDocument(userId)
            }
    }

    private fun deleteUserDocument(userId: String) {
        usersCollectionReference.document(userId).delete()
            .addOnSuccessListener {
                Log.d(TAG, "User document deleted successfully")
                deleteAuthUser(auth.currentUser!!)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting user document: $exception")
                deleteAuthUser(auth.currentUser!!)
            }
    }

    private fun deleteAuthUser(currentUser: FirebaseUser) {
        currentUser.delete()
            .addOnSuccessListener {
                Log.d(TAG, "User deleted successfully from Authentication")
                signOut()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting user from Authentication: $exception")
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
}