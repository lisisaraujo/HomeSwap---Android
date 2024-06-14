package com.example.homeswap_android.ui.personal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentPersonalBinding
import com.example.homeswap_android.databinding.FragmentUserProfileBinding
import com.example.homeswap_android.viewModels.BottomNavViewModel
import com.example.homeswap_android.viewModels.FirebaseViewModel
import com.google.firebase.firestore.toObject

class PersonalFragment : Fragment() {
    private lateinit var binding: FragmentPersonalBinding
    private val viewModel: FirebaseViewModel by activityViewModels ()
    val viewmodelBottomNav: BottomNavViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodelBottomNav.showBottomNavBar()
        binding.logoutBTN.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.loginFragment)
        }

    }
}