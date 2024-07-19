package com.example.homeswap_android.ui.personal

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSettingsBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

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
            Log.d("LogOut", userViewModel.loggedInUser.value?.email.toString())
            userViewModel.signOut()
            Log.d("LogOut", userViewModel.loggedInUser.value?.email.toString())
        }

        userViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            if (user == null) findNavController().navigate(R.id.loginFragment)
        }

        binding.deleteUserBTN.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        binding.btnMyListings.setOnClickListener {
            findNavController().navigate(R.id.myListingsFragment)
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.editProfileBTN.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

    }


    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This is a permanent action and all data related to your account will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                userViewModel.deleteUser()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}