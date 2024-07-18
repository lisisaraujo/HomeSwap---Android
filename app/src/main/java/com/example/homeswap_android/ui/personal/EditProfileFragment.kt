package com.example.homeswap_android.ui.personal

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentEditProfileBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import kotlinx.coroutines.launch


class EditProfileFragment : Fragment() {
    val TAG = "EditProfileFragment"

    private lateinit var binding: FragmentEditProfileBinding
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                userViewModel.updateProfilePicture(it) { success ->
                    if (success) {
                        Toast.makeText(
                            context,
                            "Profile picture updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.profileImage.load(it)
                    } else {
                        Toast.makeText(
                            context,
                            "Failed to update profile picture",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.loggedInUserData.collect { userData ->
                userData?.let { user ->
                    binding.fullNameET.setText(user.name)
                    binding.locationET.setText(user.city)
                    binding.editProfileDescriptionET.setText(user.bioDescription)
                }
            }
        }

        userViewModel.loggedInUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let { user ->
                val userRef = userViewModel.getUserDocumentReference(user.uid)

                binding.changePhotoBTN.setOnClickListener {
                    getContent.launch("image/*")
                }
                userRef.addSnapshotListener { value, error ->
                    if (value != null) {
                        Log.d("UserProfile", value.data.toString())
                        value.toObject(UserData::class.java)?.let { profile ->
                            if (profile.profilePic.isNotEmpty()) {
                                binding.profileImage.load(profile.profilePic) {
                                    crossfade(true)
                                    placeholder(R.drawable.ic_launcher_foreground)
                                }
                            }
                        }
                    }
                }

                binding.saveChangesBTN.setOnClickListener {
                    saveChanges()
                }

                binding.toolbar.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun saveChanges() {
        Log.d(TAG, "saveChanges called")
        val updatedName = binding.fullNameET.text.toString()
        val updatedLocation = binding.locationET.text.toString()
        val updatedDescription = binding.editProfileDescriptionET.text.toString()

        Log.d(
            TAG,
            "Updating with: Name=$updatedName, Location=$updatedLocation, Description=$updatedDescription"
        )

        val currentProfile = userViewModel.loggedInUserData.value
        if (currentProfile == null) {
            Log.e(TAG, "Current user is null")
            Toast.makeText(context, "Error: Unable to update user", Toast.LENGTH_SHORT).show()
            return
        } else {
            userViewModel.updateUserData(
                currentProfile.userID, mapOf(
                    "name" to updatedName,
                    "city" to updatedLocation,
                    "bioDescription" to updatedDescription,
                )
            ) { success ->
                if (success) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}