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
            userViewModel.signOut()
            findNavController().navigate(R.id.loginFragment)
        }

        binding.addApartmentBTN.setOnClickListener {
            userViewModel.checkEmailVerificationStatus { isVerified ->
                if (isVerified) {
                    Log.d("SettingsFragment", "Navigating to addApartmentBasicDetailsFragment")
                    findNavController().navigate(R.id.addApartmentBasicDetailsFragment)
                } else {
                    Log.d("SettingsFragment", "Showing email verification dialog")
                    showEmailVerificationDialog()
                }
            }
        }

        binding.deleteUserBTN.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        binding.btnMyListings.setOnClickListener {
            findNavController().navigate(R.id.myListingsFragment)
        }

        binding.settingsBackBTN.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun showEmailVerificationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Email Verification Required")
            .setMessage("You need to verify your email before adding an apartment. Would you like to send a verification email?")
            .setPositiveButton("Send Email") { _, _ ->
                userViewModel.loggedInUser.value?.let { user ->
                    userViewModel.sendEmailVerification(user)
                    Toast.makeText(requireContext(), "Verification email sent", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("I've verified my email") { _, _ ->
                userViewModel.checkEmailVerificationStatus { isVerified ->
                    if (isVerified) {
                        findNavController().navigate(R.id.addApartmentBasicDetailsFragment)
                    } else {
                        Toast.makeText(requireContext(), "Email not verified yet. Please try again later.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }


    private fun showDeleteAccountConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This is a permanent action and all data related to your account will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                userViewModel.deleteUser()
                findNavController().navigate(R.id.loginFragment)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}