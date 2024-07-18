package com.example.homeswap_android.ui.personal

import ApartmentAdapter
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
import com.example.homeswap_android.adapter.ReviewAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentMyProfileBinding
import com.example.homeswap_android.ui.user.UserDetailsFragmentArgs
import com.example.homeswap_android.ui.user.UserDetailsFragmentDirections
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel
import com.google.firebase.firestore.toObject

class MyProfileFragment : Fragment() {
    private lateinit var binding: FragmentMyProfileBinding
    private val TAG = "UserProfileFragment"
    private val usersViewModel: FirebaseUsersViewModel by activityViewModels()

    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val reviewsViewModel: ReviewsViewModel by activityViewModels()
    private val args: UserDetailsFragmentArgs by navArgs()

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


        usersViewModel.fetchUserData(loggedInUserID)
        apartmentViewModel.getUserApartments(loggedInUserID)

        usersViewModel.loggedInUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.profileName.text = user.name
                binding.profileImage.load(user.profilePic)
                binding.locationTV.text = user.city
                binding.rating.text = user.rating.toString()
                binding.swapsCount.text = "${user.swaps} swaps"
                binding.profileDescription.text = user.bioDescription
            }
        }

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                UserDetailsFragmentDirections.actionUserDetailsFragmentToApartmentDetailsFragment(apartment.apartmentID)
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(emptyList(), itemClickedCallback, onLikeClickListener)
        binding.userDetailsApartmentsListRV.adapter = apartmentAdapter

        apartmentViewModel.getUserApartments(loggedInUserID).addSnapshotListener{ userApartments, _ ->
            Log.d(TAG, userApartments.toString())
            apartmentAdapter.updateApartments(userApartments!!.toObjects(Apartment::class.java))
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
                binding.userReviewsTV.text = "Reviews (${userReviewsList!!.size})"
            }

        binding.userReviewsTV.setOnClickListener {
            findNavController().navigate(UserDetailsFragmentDirections.actionUserDetailsFragmentToReviewsFragment(apartmentID = null, userID = loggedInUserID))
        }


        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        binding.editProfileTV.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

    }

}
