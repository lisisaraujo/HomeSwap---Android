package com.example.homeswap_android.ui.apartment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentApartmentDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel


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

        apartmentViewModel.getApartment(apartmentID)

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->

            with(binding) {
                typeOfHomeTV.text = "Type of Home: ${apartment.typeOfHome}"
                roomsTV.text = "Number of Rooms: ${apartment.rooms}"
                maxGuestsTV.text = "Max Guests: ${apartment.maxGuests}"
                petsAllowedTV.text = "Pets Allowed: ${if (apartment.petsAllowed) "Yes" else "No"}"
                homeOfficeTV.text = "Home Office: ${if (apartment.homeOffice) "Yes" else "No"}"
                hasWifiTV.text = "Has Wifi: ${if (apartment.hasWifi) "Yes" else "No"}"
                ratingTV.text = "Rating: ${apartment.rating}"
                availabilityTV.text = "Available: ${apartment.startDate} to ${apartment.endDate}"
            }

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

            binding.userDetailsCV.setOnClickListener {
                findNavController().navigate(ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToUserDetailsFragment(user!!.userID))
            }
        }

        binding.apartmentDetailsBackBTN.setOnClickListener {
            findNavController().navigate(ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToHomeFragment(isApartments = true))
        }

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) {
            if (it == null) findNavController().navigate(R.id.loginFragment)
        }

        binding.apartmentDetailsLikeBTN.setOnClickListener {
           apartmentViewModel.currentApartment.observe(viewLifecycleOwner){currentApartment ->
              apartmentViewModel.toggleLike(currentApartment)
               if (currentApartment.liked) binding.apartmentDetailsLikeBTN.setImageResource(R.drawable.baseline_favorite_24)
                   else binding.apartmentDetailsLikeBTN.setImageResource(R.drawable.favorite_48px)
               }
           }
        }
    }