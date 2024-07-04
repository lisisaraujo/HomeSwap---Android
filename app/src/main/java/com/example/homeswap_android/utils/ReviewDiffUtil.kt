package com.example.homeswap_android.utils

import androidx.recyclerview.widget.DiffUtil
import com.example.homeswap_android.data.models.Review

class ReviewDiffUtil : DiffUtil.ItemCallback<Review>() {
    override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
        return (oldItem.reviewID == newItem.reviewID)
    }

    override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
        return (oldItem.review == newItem.review &&
                oldItem.date == newItem.date &&
                oldItem.reviewerName == newItem.reviewerName &&
                oldItem.rating == newItem.rating &&
                oldItem.reviewerProfilePic == newItem.reviewerProfilePic)
    }
}
