package com.example.homeswap_android.ui.user

import ApartmentAdapter
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
import com.example.homeswap_android.adapter.ReviewAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.databinding.FragmentUserDetailsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel
import kotlinx.coroutines.launch

class UserDetailsFragment : Fragment() {
    private lateinit var binding: FragmentUserDetailsBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private val reviewsViewModel: ReviewsViewModel by activityViewModels()
    private val args: UserDetailsFragmentArgs by navArgs()

    private lateinit var apartmentAdapter: ApartmentAdapter
    private lateinit var reviewsAdapter: ReviewAdapter

    val TAG = "UserDetailsFragment"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reviewsAdapter = ReviewAdapter()
        binding.userDetailsReviewsRV.adapter = reviewsAdapter

        val userID = args.userID!!

        userViewModel.fetchSelectedUserData(userID)
        apartmentViewModel.getUserApartments(userID)

        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.selectedUserData.collect { user ->
                if (user != null) {
                    Log.d(TAG, user.location)
                    binding.profileName.text = user.name
                    binding.profileImage.load(user.profilePic)
                    binding.locationTV.text = user.location
                    binding.rating.text = user.rating.toString()
                    binding.userDetailsReviewsTV.text = "${user.reviewsCount} reviews"
                    binding.userProfileDescriptionTV.text = user.bioDescription
                    binding.toolbar.title = "${user.name}'s Profile"
                }
            }
        }

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                UserDetailsFragmentDirections.actionUserDetailsFragmentToApartmentDetailsFragment(
                    apartment.apartmentID
                )
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(emptyList(), itemClickedCallback, onLikeClickListener)
        binding.userDetailsApartmentsListRV.adapter = apartmentAdapter

        apartmentViewModel.getUserApartments(userID).addSnapshotListener { userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            apartmentAdapter.updateApartments(userApartments!!.toObjects(Apartment::class.java))
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()

        }


        reviewsViewModel.getUserReviews(userID)
            .addSnapshotListener { userReviews, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching apartment reviews", error)
                    return@addSnapshotListener
                }
                val userReviewsList =
                    userReviews?.toObjects(Review::class.java)
                Log.d(TAG, userReviewsList.toString())
                reviewsAdapter.submitList(userReviewsList)
                binding.userDetailsReviewsTV.text = "Reviews (${userReviewsList!!.size})"
            }

        binding.seeAllReviews.setOnClickListener {
            findNavController().navigate(
                UserDetailsFragmentDirections.actionUserDetailsFragmentToReviewsFragment(
                    apartmentID = null,
                    userID = userID
                )
            )
        }

        binding.seeAllUserApartments.setOnClickListener {
            findNavController().navigate(
                UserDetailsFragmentDirections.actionUserDetailsFragmentToUserApartmentsFragment(
              userID
                )
            )
        }


    }
}