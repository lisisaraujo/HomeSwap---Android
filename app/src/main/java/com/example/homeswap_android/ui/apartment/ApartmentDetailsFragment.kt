package com.example.homeswap_android.ui.apartment

import android.annotation.SuppressLint
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
import com.example.homeswap_android.databinding.FragmentApartmentDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel


class ApartmentDetailsFragment : Fragment() {

    val TAG = "ApartmentDetailsFragment"

    private lateinit var binding: FragmentApartmentDetailsBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: ApartmentDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentID = args.apartmentID

        apartmentViewModel.getApartment(apartmentID)

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->

            with(binding) {
                typeOfHomeTV.text = (if(apartment.typeOfHome.isNotBlank()) apartment.typeOfHome else "-").toString()
                roomsTV.text = "Rooms: ${apartment.rooms}"
                maxGuestsTV.text = "Guests: ${apartment.maxGuests}"
                petsAllowedTV.text = if (apartment.petsAllowed) "Pets Allowed" else "No Pets Allowed"
                homeOfficeTV.text = if (apartment.homeOffice) "Home Office" else "No Home Office"
                hasWifiTV.text = if (apartment.hasWifi) "Wifi" else "No Wifi"
                ratingTV.text = "Rating: ${apartment.rating}"
                availabilityTV.text = "Available: ${apartment.startDate} to ${apartment.endDate}"
            }

            if (apartment != null) {
                binding.apartmentTitleTV.text = apartment.title

                apartmentViewModel.getApartmentFirstPicture(apartment.apartmentID, apartment.userID).observe(viewLifecycleOwner) { imageUrl ->
                    Log.d("apartmentFirstPic", imageUrl ?: "No image URL")
                    if (imageUrl != null) {
                        binding.apartmentImageIV.load(imageUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_foreground)
                            error(R.drawable.ic_launcher_foreground)
                        }
                    } else {
                        binding.apartmentImageIV.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }

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

        binding.apartmentImageIV.setOnClickListener {
            findNavController().navigate(ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToApartmentPicturesFragment(apartmentID))
        }
        }
    }