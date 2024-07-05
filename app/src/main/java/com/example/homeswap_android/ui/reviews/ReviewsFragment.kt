package com.example.homeswap_android.ui.reviews

import ApartmentAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ReviewAdapter
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.databinding.FragmentApartmentsListHomeBinding
import com.example.homeswap_android.databinding.FragmentReviewBinding
import com.example.homeswap_android.ui.apartment.ApartmentDetailsFragmentArgs
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.ReviewsViewModel

class ReviewsFragment : Fragment() {
    val TAG = "ReviewFragment"

    private val args: ReviewsFragmentArgs by navArgs()
    private lateinit var binding: FragmentReviewBinding
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewsViewModel: ReviewsViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentID = args.apartmentID
        val userID = args.userID

        reviewAdapter = ReviewAdapter()
        binding.reviewRV.adapter = reviewAdapter

        if (apartmentID != null) {
            reviewsViewModel.getApartmentReviews(apartmentID)
                .addSnapshotListener { apartmentReviews, error ->
                    if (error != null) {
                        Log.e(TAG, "Error fetching apartment reviews", error)
                        return@addSnapshotListener
                    }
                    val apartmentReviewsList = apartmentReviews?.toObjects(Review::class.java)
                    Log.d(TAG, apartmentReviewsList.toString())
                    reviewAdapter.submitList(apartmentReviewsList)
                }
        } else {
            reviewsViewModel.getUserReviews(userID!!).addSnapshotListener { userReviews, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching user reviews", error)
                    return@addSnapshotListener
                }
                Log.d(TAG, userReviews.toString())
                reviewAdapter.submitList(userReviews!!.toObjects(Review::class.java))
            }
        }

        binding.addReviewFAB.setOnClickListener {
            findNavController().navigate(
                ReviewsFragmentDirections.actionReviewsFragmentToAddReviewFragment(
                    apartmentID = apartmentID, userID = userID
                )
            )
        }

        binding.toolbar.setNavigationOnClickListener{
            findNavController().navigateUp()
        }
    }
}

