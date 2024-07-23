package com.example.homeswap_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.databinding.ReviewListItemBinding


class ReviewAdapter(
) : ListAdapter<Review, ReviewAdapter.ItemViewHolder>(ReviewDiffCallback()) {

    inner class ItemViewHolder(val binding: ReviewListItemBinding) :
        RecyclerView.ViewHolder(binding.root)


    fun sortReviews() {
        val sortedList = currentList.sortedBy { it.date }
        submitList(sortedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ReviewListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = currentList[position]

        holder.binding.nameTV.text = item.reviewerName
        holder.binding.reviewTV.text = item.review
        holder.binding.dateTV.text = item.date
        holder.binding.profileIV.load(item.reviewerProfilePic)
        holder.binding.ratingBar.rating = item.rating!!
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
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
}