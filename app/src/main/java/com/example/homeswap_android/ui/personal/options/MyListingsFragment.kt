package com.example.homeswap_android.ui.personal.options

import EditApartmentAdapter
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
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentMyListingsBinding
import com.example.homeswap_android.ui.user.UserDetailsFragmentArgs
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class MyListingsFragment : Fragment() {
    private lateinit var binding: FragmentMyListingsBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

    private lateinit var editApartmentAdapter: EditApartmentAdapter

    val TAG = "MyListingsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyListingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userID = userViewModel.loggedInUser.value?.uid

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                MyListingsFragmentDirections.actionMyListingsFragmentToEditApartmentFragment(apartment.apartmentID)
            )
        }


        editApartmentAdapter = EditApartmentAdapter(itemClickedCallback)

        binding.myListingsRV.adapter = editApartmentAdapter

        apartmentViewModel.getUserApartments(userID!!).addSnapshotListener { userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            editApartmentAdapter.submitList(userApartments!!.toObjects(Apartment::class.java))
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()

        }

        binding.fabAddListing.setOnClickListener {
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

}