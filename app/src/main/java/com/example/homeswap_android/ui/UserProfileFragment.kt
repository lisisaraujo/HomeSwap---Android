package com.example.homeswap_android.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentUserProfileBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel
import com.google.firebase.firestore.toObject

class UserProfileFragment : Fragment() {
    private lateinit var binding: FragmentUserProfileBinding
    private val viewModel: FirebaseViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.userDataDocumentReference?.addSnapshotListener { value, error ->
            val user = value?.toObject<UserData>()
            binding.userProfileNameTV.text = user?.name
            binding.userProfileEmailTV.text = user?.email
            binding.userProfileReviewsTV.text = user?.reviews?.size.toString()
            binding.userProfileIV.load(user?.profilePic)
        }

        viewModel.currentUser.observe(viewLifecycleOwner){
            if(it == null) findNavController().navigate(R.id.loginFragment)
        }

        binding.logoutBTN.setOnClickListener {
            viewModel.signOut()
        }
        binding.deleteAllUsersBTN.setOnClickListener {
            viewModel.deleteAllUsers()
        }
    }
}