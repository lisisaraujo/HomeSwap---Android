package com.example.homeswap_android.ui.apartment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.example.homeswap_android.utils.Utils.getLeftToRightNavOptions
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

                binding.toolbar.title = "${apartment.title}"
                Log.d(TAG, apartment.city)
                with(binding) {
                    //type of home
                    if (apartment.typeOfHome.isNotBlank()) {
                        typeOfHomeTV.text = apartment.typeOfHome
                        typeOfHomeTV.visibility = View.VISIBLE
                    } else {
                        typeOfHomeTV.visibility = View.GONE
                    }

                    //rooms
                    if (apartment.rooms != null) {
                        roomsTV.text = "Rooms: ${apartment.rooms}"
                        roomsTV.visibility = View.VISIBLE
                    } else {
                        roomsTV.visibility = View.GONE
                    }

                    //max guests
                    if (apartment.maxGuests!= null) {
                        maxGuestsTV.text = "Guests: ${apartment.maxGuests}"
                        maxGuestsTV.visibility = View.VISIBLE
                    } else {
                        maxGuestsTV.visibility = View.GONE
                    }

                    //pets allowed
                    petsAllowedTV.text = if (apartment.petsAllowed) "Pets Allowed" else "No Pets Allowed"
                    petsAllowedTV.visibility = View.VISIBLE

                    //home Office
                    homeOfficeTV.text = if (apartment.homeOffice) "Home Office" else "No Home Office"
                    homeOfficeTV.visibility = View.VISIBLE

                    //wifi
                    hasWifiTV.text = if (apartment.hasWifi) "Wifi" else "No Wifi"
                    hasWifiTV.visibility = View.VISIBLE

                    //rating
                    if (apartment.rating != null) {
                        apartmentRatingTV.text = "Rating: ${apartment.rating}"
                        apartmentRatingTV.visibility = View.VISIBLE
                    } else {
                        apartmentRatingTV.visibility = View.GONE
                    }

                    //availability
                        availabilityTV.text = "Available: ${apartment.startDate} to ${apartment.endDate}"

                    //apartment Title
                        apartmentTitleTV.text = apartment.title


                    //cover Picture
                    if (apartment.coverPicture.isNotBlank()) {
                        coverPictureIV.load(apartment.coverPicture)
                    }
            }
            }

            //user ID
            val userID = apartment.userID
            userViewModel.fetchSelectedUserData(userID)


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
                    binding.reviewsTitleTV.text = "Reviews (${apartmentReviewsList!!.size})"
                }


            viewLifecycleOwner.lifecycleScope.launch {
                userViewModel.selectedUserData.collect { user ->
                    if (user != null) {
                        binding.profileName.text = user.name
                        binding.profileImage.load(user.profilePic)
                        binding.userLocationTV.text = user.location
                        binding.hostReviewsTV.text = "Reviews (${user.reviewsCount})"
                        binding.userDetailsRatingsTV.text = user.rating.toString()

                        binding.userDetailsCV.setOnClickListener {
                            findNavController().navigate(
                                ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToUserDetailsFragment(
                                    user.userID
                                )
                            )
                        }

                        binding.contactHostButton.setOnClickListener {
                            showEmailDialog(user.email)
                        }
                    }
                }
            }

            binding.toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            binding.coverPictureIV.setOnClickListener {
                findNavController().navigate(
                    ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToApartmentPicturesFragment(
                        apartmentID
                    ),
                    getLeftToRightNavOptions()
                )
            }

            binding.imageGalleryBTN.setOnClickListener {
                findNavController().navigate(
                    ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToApartmentPicturesFragment(
                        apartmentID
                    ),
                    getLeftToRightNavOptions()
                )
            }


            binding.seeAllReviewsTV.setOnClickListener {
                findNavController().navigate(
                    ApartmentDetailsFragmentDirections.actionApartmentDetailsFragmentToReviewsFragment(
                        apartmentID = apartmentID,
                        userID = null
                    )
                )
            }

            binding.descriptionContentTV.text = apartment.description
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

    private fun showEmailDialog(email: String) {
        val builder = AlertDialog.Builder(requireContext(), R.style.RoundedAlertDialog)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_rounded, null)

        val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val messageTextView = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val button = dialogView.findViewById<Button>(R.id.dialogButton)

        titleTextView.text = "Contact Host"
        messageTextView.text = "Email: $email"

        builder.setView(dialogView)

        val dialog = builder.create()

        button.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}