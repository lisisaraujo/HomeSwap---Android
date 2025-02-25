package com.example.homeswap_android.ui.personal

import ApartmentAdapter
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ReviewAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentMyProfileBinding
import com.example.homeswap_android.utils.Utils.openExternalLink
import com.example.homeswap_android.utils.Utils.showRedirectionConfirmationDialog
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel
import kotlinx.coroutines.launch

class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private val TAG = "UserProfileFragment"
    private val usersViewModel: FirebaseUsersViewModel by activityViewModels()

    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val reviewsViewModel: ReviewsViewModel by activityViewModels()

    private lateinit var apartmentAdapter: ApartmentAdapter
    private lateinit var reviewsAdapter: ReviewAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        usersViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            if (user == null) findNavController().navigate(R.id.loginFragment)
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loggedInUserID = usersViewModel.loggedInUser.value!!.uid

        reviewsAdapter = ReviewAdapter()
        binding.userDetailsReviewsRV.adapter = reviewsAdapter

        usersViewModel.fetchSelectedUserData(loggedInUserID)
        apartmentViewModel.getUserApartments(loggedInUserID)

        usersViewModel.fetchSelectedUserData(loggedInUserID)

        viewLifecycleOwner.lifecycleScope.launch {
            usersViewModel.loggedInUserData.collect { user ->
                if (user != null) {
                    updateUI(user)
                    val userOrigin = user.location.split(",").firstOrNull()?.trim()

                    Log.d(TAG, "user origin split: $userOrigin")
                    Log.d(TAG, "user location: ${user.location}")
                }
            }
        }

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(MyProfileFragmentDirections.actionUserProfileFragmentToApartmentDetailsFragment(apartment.apartmentID))
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(itemClickedCallback, onLikeClickListener)
        binding.userDetailsApartmentsListRV.adapter = apartmentAdapter

        apartmentViewModel.getUserApartments(loggedInUserID)
            .addSnapshotListener { userApartments, _ ->
                Log.d(TAG, userApartments.toString())
                apartmentAdapter.submitList(userApartments!!.toObjects(Apartment::class.java))
            }


        reviewsViewModel.getUserReviews(loggedInUserID)
            .addSnapshotListener { userReviews, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching apartment reviews", error)
                    return@addSnapshotListener
                }
                val userReviewsList =
                    userReviews?.toObjects(Review::class.java)
                Log.d(TAG, userReviewsList.toString())
                reviewsAdapter.submitList(userReviewsList)
                binding.myProfileReviewsTV.text = "${userReviewsList!!.size}"
            }

        binding.seeAllReviewsTV.setOnClickListener {
            findNavController().navigate(
                MyProfileFragmentDirections.actionUserProfileFragmentToReviewsFragment(
                    apartmentID = null,
                    userID = loggedInUserID
                )
            )
        }

        binding.seeAllReviews.setOnClickListener {
            findNavController().navigate(R.id.myListingsFragment
                )
        }


        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        redirectToExternalApp()

    }

    private fun updateUI(user: UserData) {
        binding.profileName.text = user.name
        binding.profileImage.load(user.profilePic)
        binding.locationTV.text = user.location
        binding.rating.text = user.rating.toString()
        binding.reviewsTitleTV.text = "Reviews (${user.reviewsCount})"
        binding.myProfileDescriptionTV.text = user.bioDescription
    }
    private fun redirectToExternalApp(){
        binding.instagramIB.setOnClickListener {
            showRedirectionConfirmationDialog(
                requireContext(),
                "http://instagram.com/_u/madonna",
                "com.instagram.android"
            )
        }

        binding.instagramIB.setOnClickListener {
            showRedirectionConfirmationDialog(
                requireContext(),
                "http://instagram.com/_u/madonna",
                "com.linkedIn.android"
            )
        }

        binding.linkedInIB.setOnClickListener {
            showRedirectionConfirmationDialog(
                requireContext(),
                "https://www.linkedin.com/in/lisisaraujo/",
                "com.linkedin.android"
            )
        }

        binding.facebookIB.setOnClickListener {
            showRedirectionConfirmationDialog(
                requireContext(),
                "http://facebook.com/your_profile",
                "com.facebook.katana"
            )
        }

        binding.twitterIB.setOnClickListener {
            showRedirectionConfirmationDialog(
                requireContext(),
                "http://twitter.com/your_profile",
                "com.twitter.android"
            )
        }

    }

}
