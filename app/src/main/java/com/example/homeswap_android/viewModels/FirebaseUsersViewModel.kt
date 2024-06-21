package com.example.homeswap_android.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class FirebaseUsersViewModel : ViewModel() {

    //Firebase Dienst Instanzen laden
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val usersCollectionReference = firestore.collection("users")
    var userDataDocumentReference: DocumentReference? = null
    val storage = FirebaseStorage.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private val _currentUserData = MutableLiveData<UserData?>()
    val currentUserData: LiveData<UserData?>
        get() = _currentUserData

    private val _users = MutableLiveData<List<UserData>>()
    val users: LiveData<List<UserData>>
        get() = _users

    val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean>
        get() = _loginResult

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean>
        get() = _registerResult


    init {
        setupUserEnv()
    }

    private fun setupUserEnv() {
        val user = auth.currentUser
        _currentUser.postValue(user)
        if (user != null) {
            //Immer wenn der User eingeloggt ist muss diese Variable definiert sein
            userDataDocumentReference = usersCollectionReference.document(user.uid)
            Log.d("NewUser", user.email.toString())

        }
    }

    fun setProfile(profile: UserData) {
        if (userDataDocumentReference == null) {
            //Funktion abbrechen
            return
        }
        userDataDocumentReference!!.set(profile)
        userDataDocumentReference!!.update("userID", userDataDocumentReference!!.id)
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

                // create the user profile
                usersCollectionReference.document(user.uid).set(userData)
                    .addOnSuccessListener {
                        Log.d("FirebaseViewModel", "New user profile created successfully")
                        _registerResult.value = true
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FirebaseViewModel", "Error creating new user profile: $exception")
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
                        userDataDocumentReference?.update("profilePic", imageUrl)
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

        // delete user's apartments and their pictures
        firestore.collection("apartments").whereEqualTo("userID", userId).get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val apartment = document.toObject(Apartment::class.java)
                    apartment?.let { deleteApartmentAndPictures(it) }
                }

                // delete user's profile picture
                val profilePicRef = storage.reference.child("images/$userId/profilePic")
                profilePicRef.delete().addOnFailureListener { exception ->
                    Log.e(TAG, "Error deleting profile picture: $exception")
                }

                // delete user document from firestore
                usersCollectionReference.document(userId).delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "User document deleted successfully")

                        // Step 4: Delete user authentication
                        currentUser.delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "User deleted successfully")
                                // Perform any additional cleanup or UI updates
                                signOut()
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error deleting user: $exception")
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "Error deleting user document: $exception")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error fetching user's apartments: $exception")
            }
    }

    private fun deleteApartmentAndPictures(apartment: Apartment) {
        // delete apartment pictures from storage
        apartment.pictures.forEach { pictureUrl ->
            val pictureRef = storage.getReferenceFromUrl(pictureUrl)
            pictureRef.delete().addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting apartment picture: $exception")
            }
        }

        // delete apartment document from Firestore
        firestore.collection("apartments").document(apartment.apartmentID).delete()
            .addOnSuccessListener {
                Log.d(TAG, "Apartment deleted successfully: ${apartment.apartmentID}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error deleting apartment: $exception")
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