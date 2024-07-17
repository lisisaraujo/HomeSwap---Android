package com.example.homeswap_android.ui.personal

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentMyProfileBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.firebase.firestore.toObject

class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private val TAG = "UserProfileFragment"
    private val usersViewModel: FirebaseUsersViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        usersViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            if (user == null) findNavController().navigate(R.id.loginFragment)
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersViewModel.loggedInUser.observe(viewLifecycleOwner) { currentUser ->
            currentUser.let {
                val userRef = usersViewModel.getUserDocumentReference(currentUser!!.uid)
                userRef.addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d(TAG, "User not found.")
                        return@addSnapshotListener
                    }

                    val user = value?.toObject<UserData>()
                    user?.let {
                        binding.profileName.text = it.name
                        binding.userReviewsTV.text = "${it.reviewsCount} reviews"
                        binding.profileImage.load(it.profilePic)
                        binding.rating.text = it.rating.toString()
                        binding.locationTV.text = it.city
                        binding.profileDescription.text = it.bioDescription
                    }
                }
            }

        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        binding.editProfileTV.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

    }

}
