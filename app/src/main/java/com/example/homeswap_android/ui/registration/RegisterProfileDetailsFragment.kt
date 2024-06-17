package com.example.homeswap_android.ui.registration

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentRegisterProfileDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel

class RegisterProfileDetailsFragment : Fragment() {
    private lateinit var binding: FragmentRegisterProfileDetailsBinding
    private val viewModel: FirebaseViewModel by activityViewModels()

    private val getContent =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                viewModel.uploadImage(it)
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

        binding.uploadProfilePicIV.setOnClickListener {
            getContent.launch("image/*")
        }

        viewModel.userDataDocumentReference?.addSnapshotListener { value, error ->
            if (value != null) {
                Log.d("UserProfile", value.data.toString())
                value.toObject(UserData::class.java)?.let { profile ->
                    if (profile.profilePic.isNotEmpty()) {
                        binding.uploadProfilePicIV.load(profile.profilePic) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_foreground)
                        }
                    }
                }
            }
        }

        binding.continueBTN.setOnClickListener {
            findNavController().navigate(R.id.usersListHomeFragment)
        }
    }
}