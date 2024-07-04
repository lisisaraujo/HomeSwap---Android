package com.example.homeswap_android.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.data.models.Review
import com.example.homeswap_android.databinding.ReviewListItemBinding
import com.example.homeswap_android.utils.ReviewDiffUtil


class ReviewAdapter(
) : ListAdapter<Review, ReviewAdapter.ItemViewHolder>(ReviewDiffUtil()) {

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
}