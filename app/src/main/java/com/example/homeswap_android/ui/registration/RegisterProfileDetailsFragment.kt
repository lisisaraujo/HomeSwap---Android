package com.example.homeswap_android.ui.registration

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
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentRegisterProfileDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class RegisterProfileDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRegisterProfileDetailsBinding
    private val usersViewModel: FirebaseUsersViewModel by activityViewModels()

    private val getContent =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                usersViewModel.uploadImage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersViewModel.loggedInUser.observe(viewLifecycleOwner) { firebaseUser ->
            firebaseUser?.let { user ->
                val userID = user.uid
                val userRef = usersViewModel.getUserDocumentReference(userID)

                binding.uploadProfilePicIV.setOnClickListener {
                    getContent.launch("image/*")
                }

                userRef.addSnapshotListener { value, error ->
                    if (value != null) {
                        Log.d("UserProfile", value.data.toString())
                        value.toObject(UserData::class.java)?.let { profile ->
                            if (profile.profilePic.isNotEmpty()) {
                                binding.uploadProfilePicIV.load(profile.profilePic) {
                                    crossfade(true)
                                    placeholder(R.drawable.ic_launcher_foreground)
                                }
                            }
                            //pre-fill city and country if available
                            binding.etCity.setText(profile.city)
                            binding.etCountry.setText(profile.country)
                        }
                    }
                }

                binding.continueBTN.setOnClickListener {
                    val city = binding.etCity.text.toString().trim()
                    val country = binding.etCountry.text.toString().trim()
                    val bioDescription = binding.bioDescriptionET.text.toString()

                    if (city.isNotEmpty() && country.isNotEmpty()) {
                        usersViewModel.updateUserData(userID, mapOf(
                            "city" to city,
                            "country" to country,
                            "bioDescription" to bioDescription
                        ))
                        findNavController().navigate(R.id.homeFragment)
                    } else {
                        Toast.makeText(context, "Fill out all data to continue", Toast.LENGTH_LONG).show()

                    }
                }
            } ?: Log.e("RegisterProfileDetailsFragment", "Current user is null")
        }
    }
}
