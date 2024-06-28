package com.example.homeswap_android.ui.personal

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
import com.example.homeswap_android.databinding.FragmentRegisterProfileDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.firebase.firestore.ListenerRegistration
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->

            if (currentUser == null) {
                findNavController().navigate(R.id.loginFragment)
            } else {
                val userRef = usersViewModel.getUserDocumentReference(currentUser.uid)
                userRef.addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d(TAG, "User not found.")
                        return@addSnapshotListener
                    }

                    val user = value?.toObject<UserData>()
                    user?.let {
                        binding.userProfileNameTV.text = it.name
                        binding.userProfileEmailTV.text = it.email
                        binding.userProfileReviewsTV.text = it.reviews?.size.toString()
                        binding.userProfileIV.load(it.profilePic)
                    }
                }
            }
        }

        binding.menuButton.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

    }

}
