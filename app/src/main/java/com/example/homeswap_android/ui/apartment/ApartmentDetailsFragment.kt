package com.example.homeswap_android.ui.apartment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ReviewAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.databinding.FragmentApartmentDetailsBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel
import kotlinx.coroutines.launch


class ApartmentDetailsFragment : Fragment() {

    val TAG = "ApartmentDetailsFragment"

    private lateinit var binding: FragmentApartmentDetailsBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: ApartmentDetailsFragmentArgs by navArgs()
    private lateinit var reviewsAdapter: ReviewAdapter
    private val reviewsViewModel: ReviewsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentDetailsBinding.inflate(inflater, container, false)
        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            if (apartment == null) findNavController().navigate(R.id.loginFragment)
            else Utils.updateLikeButton(binding.apartmentDetailsLikeBTN, apartment.liked)
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewsAdapter = ReviewAdapter()
        binding.reviewsRV.adapter = reviewsAdapter

        val apartmentID = args.apartmentID

        apartmentViewModel.getApartment(apartmentID).observe(viewLifecycleOwner) { apartment ->
            if (apartment != null) {
                with(binding) {
                    typeOfHomeTV.text =
                        (if (apartment.typeOfHome.isNotBlank()) apartment.typeOfHome else "-").toString()
                    roomsTV.text = "Rooms: ${apartment.rooms}"
                    maxGuestsTV.text = "Guests: ${apartment.maxGuests}"
                    petsAllowedTV.text =
                        if (apartment.petsAllowed) "Pets Allowed" else "No Pets Allowed"
                    homeOfficeTV.text =
                        if (apartment.homeOffice) "Home Office" else "No Home Office"
                    hasWifiTV.text = if (apartment.hasWifi) "Wifi" else "No Wifi"
                    apartmentDetailsRatingsTV.text = "Rating: ${apartment.rating}"
                    availabilityTV.text =
                        "Available: ${apartment.startDate} to ${apartment.endDate}"

                    apartmentTitleTV.text = apartment.title
                    coverPictureIV.load(apartment.coverPicture)
                    Log.d(TAG, "ApartmentCoverPic: ${apartment.coverPicture}")

                    val userID = apartment.userID
                    userViewModel.fetchSelectedUserData(userID)
                }
            }

            reviewsViewModel.getApartmentReviews(apartmentID)
                .addSnapshotListener { apartmentReviews, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching apartment reviews", error)
                        return@addSnapshotListener
                    }
                    val apartmentReviewsList =
                        apartmentReviews?.toObjects(Review::class.java)
                    Log.d(TAG, apartmentReviewsList.toString())
                    reviewsAdapter.submitList(apartmentReviewsList)
                    binding.reviewsTV.text = "Reviews (${apartmentReviewsList!!.size})"
                }


            viewLifecycleOwner.lifecycleScope.launch {
                userViewModel.loggedInUserData.collect { user ->
                    if (user != null) {
                        binding.profileName.text = user.name
                        binding.profileImage.load(user.profilePic)
                    }

                    binding.userDetailsCV.setOnClickListener {
                        findNavController().navigate(
                            ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToUserDetailsFragment(
                                user!!.userID
                            )
                        )
                    }
                }
            }

            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigate(
                    ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToHomeFragment(
                        isApartments = true
                    )
                )
            }

            binding.coverPictureIV.setOnClickListener {
                findNavController().navigate(
                    ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToApartmentPicturesFragment(
                        apartmentID
                    )
                )
            }


            binding.reviewsTV.setOnClickListener {
                findNavController().navigate(
                    ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToReviewsFragment(
                        apartmentID = apartmentID,
                        userID = null
                    )
                )
            }

            binding.apartmentDetailsDescriptionTV.text = apartment.description
            binding.locationTV.text = apartment.city

        }

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            apartment.let {
                binding.apartmentDetailsLikeBTN.setOnClickListener {
                    apartmentViewModel.toggleLike(apartment!!)
                    Utils.updateLikeButton(binding.apartmentDetailsLikeBTN, apartment.liked)
                }
            }
        }

    }
}