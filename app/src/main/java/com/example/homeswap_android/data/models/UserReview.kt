package com.example.homeswap_android.data.models

data class UserReview(
    val userID: String = "",
    override val reviewType: String = "",
    override val reviewID: String = "",
    override val reviewerID: String = "",
    override val reviewerName: String = "",
    override val review: String = "",
    override val date: String = "",
    override val rating: Float? = null,
    override val reviewerProfilePic: String = ""
) : Review(
    reviewType,
    reviewID,
    reviewerID,
    reviewerName,
    review,
    date,
    rating,
    reviewerProfilePic
)