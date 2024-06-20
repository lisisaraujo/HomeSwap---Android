package com.example.homeswap_android.ui.apartment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentApartmentDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.firebase.firestore.toObject


class ApartmentDetailsFragment : Fragment() {

    val TAG = "ApartmentDetailsFragment"

    private lateinit var binding: FragmentApartmentDetailsBinding
    private val apartmentViewModel: FirebaseApartmentViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: ApartmentDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentID = args.apartmentID

        apartmentViewModel.fetchApartment(apartmentID)

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            if (apartment != null) {
                binding.apartmentTitleTV.text = apartment.title
                binding.apartmentImageIV.load(apartment.pictures.firstOrNull())

                val userID = apartment.userID
                userViewModel.fetchUserData(userID)
            }
        }

        userViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.userNameTV.text = user.name
                binding.profilePicIV.load(user.profilePic)
            }
        }

        binding.apartmentDetailsBackBTN.setOnClickListener {
            findNavController().navigate(ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToHomeFragment(isApartments = true))
        }

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) {
            if (it == null) findNavController().navigate(R.id.loginFragment)
        }
    }
}