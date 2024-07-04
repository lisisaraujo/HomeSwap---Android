package com.example.homeswap_android.ui.reviews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.data.models.ApartmentReview
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.data.models.UserReview
import com.example.homeswap_android.databinding.FragmentAddReviewBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//package com.example.homeswap_android.ui.reviews


class AddReviewFragment : Fragment() {
val TAG = "AddReviewFragment"

    private lateinit var binding: FragmentAddReviewBinding
    private val reviewsViewModel: ReviewsViewModel by activityViewModels()
    private val usersViewModel: FirebaseUsersViewModel by activityViewModels()
    private val args: AddReviewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentID = args.apartmentID
        val userID = args.userID

        binding.submitButton.setOnClickListener {
            val review = binding.reviewET.text.toString()
            val rating = binding.ratingBar.rating


            val newReview = if (apartmentID != null) {
                ApartmentReview(
                    reviewerID = usersViewModel.currentUser.value!!.uid,
                    reviewerName = usersViewModel.currentUserData.value!!.name,
                    review = review,
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    rating = rating,
                    reviewerProfilePic = usersViewModel.currentUserData.value!!.profilePic,
                    apartmentID = apartmentID!!
                )
            } else {
                UserReview(
                    reviewerID = usersViewModel.currentUser.value!!.uid,
                    reviewerName = usersViewModel.currentUserData.value!!.name,
                    review = review,
                    date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                    rating = rating,
                    reviewerProfilePic = usersViewModel.currentUserData.value!!.profilePic,
                    userID = userID!!
                )
            }
            reviewsViewModel.addReview(newReview)
        }


        reviewsViewModel.newAddedReview.observe(viewLifecycleOwner){review ->
            review.let {
                Log.d(TAG, review.review)
                findNavController().navigateUp()
            }
        }
    }
}