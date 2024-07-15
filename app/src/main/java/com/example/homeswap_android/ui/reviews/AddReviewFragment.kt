package com.example.homeswap_android.ui.reviews

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.databinding.FragmentAddReviewBinding
import com.example.homeswap_android.utils.Utils.dateFormat
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel
import java.util.Date


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
                Log.d("ApartmentID", apartmentID)
                Review(
                    reviewType = "apartment",
                    reviewerID = usersViewModel.loggedInUser.value!!.uid,
                    reviewerName = usersViewModel.loggedInUserData.value!!.name,
                    review = review,
                    date = dateFormat.format(Date()),
                    rating = rating,
                    reviewerProfilePic = usersViewModel.loggedInUserData.value!!.profilePic,
                    destinationID = apartmentID
                )
            } else {
                Log.d("UserID", userID!!)
                Review(
                    reviewType = "user",
                    reviewerID = usersViewModel.loggedInUser.value!!.uid,
                    reviewerName = usersViewModel.loggedInUserData.value!!.name,
                    review = review,
                    date = dateFormat.format(Date()),
                    rating = rating,
                    reviewerProfilePic = usersViewModel.loggedInUserData.value!!.profilePic,
                    destinationID = userID
                )
            }

            reviewsViewModel.addReview(
                newReview,
                onSuccess = {
                    Toast.makeText(
                        context,
                        "Review submitted successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                },
                onFailure = { exception ->

                    Log.e("AddReviewFragment", "Failed to add review: ${exception.message}")
                    Toast.makeText(
                        context,
                        "Failed to add review. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        usersViewModel.loggedInUserData.observe(viewLifecycleOwner){user ->
            binding.reviewProfilePicIV.load(user?.profilePic)
        }

    }


}