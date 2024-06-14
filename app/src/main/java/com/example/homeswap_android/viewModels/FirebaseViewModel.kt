package com.example.homeswap_android.viewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.homeswap_android.data.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.security.AccessController.getContext


class FirebaseViewModel : ViewModel() {

    private lateinit var context: Context

    //Firebase Dienst Instanzen laden
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    val usersCollectionReference = firestore.collection("users")
    var userDataDocumentReference: DocumentReference? = null
    val storage = FirebaseStorage.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?>
        get() = _currentUser

    private val _users = MutableLiveData<List<UserData>>()
    val users: LiveData<List<UserData>>
        get() = _users

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean>
        get() = _registrationSuccess

    init {
        setupUserEnv()
    }

    private fun setupUserEnv() {
        val user = auth.currentUser
        _currentUser.postValue(user)
        if (user != null) {
            //Immer wenn der User eingeloggt ist muss diese Variable definiert sein
            userDataDocumentReference = usersCollectionReference.document(user.uid)
        }
    }

    fun setProfile(profile: UserData) {
        if (userDataDocumentReference == null) {
            //Funktion abbrechen
            return
        }
        userDataDocumentReference!!.set(profile)
    }


    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            setupUserEnv()
        }
    }

    fun register(email: String, password: String, userData: UserData) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                setupUserEnv()
                val user = authResult.user!!

                // send email verification
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                        }
                    }.addOnFailureListener { exception ->
                        Log.d(TAG, "onFailure: Email not sent" + exception.message)
                    }

                // Wait for the user to confirm their email before creating the user profile
                val emailConfirmedListener = object : FirebaseAuth.IdTokenListener {
                    override fun onIdTokenChanged(p0: FirebaseAuth) {
                        val currentUser = auth.currentUser
                        if (currentUser != null && currentUser.isEmailVerified) {
                            // User has confirmed their email, create the user profile
                            usersCollectionReference.document(currentUser.uid).set(userData)
                                .addOnSuccessListener {
                                    Log.d(
                                        "FirebaseViewModel",
                                        "New user profile created successfully"
                                    )
                                    fetchUsers()
                                    auth.removeIdTokenListener(this)
                                    _registrationSuccess.postValue(true)
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "FirebaseViewModel",
                                        "Error creating new user profile: $exception"
                                    )
                                    auth.removeIdTokenListener(this)
                                    _registrationSuccess.postValue(false)
                                }
                        }
                    }
                }

                // Add the email confirmed listener
                auth.addIdTokenListener(emailConfirmedListener)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error creating user: $exception")
                _registrationSuccess.postValue(false)
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

    fun deleteAllUsers() {
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

}