package com.example.homeswap_android.ui.personal.options

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSettingsBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: FirebaseUsersViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutBTN.setOnClickListener {
            viewModel.signOut()
            findNavController().navigate(R.id.loginFragment)
        }

        binding.addApartmentBTN.setOnClickListener {
            findNavController().navigate(R.id.addApartmentFragment)
        }
    }
}