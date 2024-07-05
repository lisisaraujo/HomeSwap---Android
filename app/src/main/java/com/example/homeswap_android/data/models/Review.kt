package com.example.homeswap_android.data.models

data class Review(
    val destinationID: String = "",
    val reviewType: String = "",
    val reviewID: String = "",
    val reviewerID: String = "",
    val reviewerName: String = "",
    val review: String = "",
    val date: String = "",
    val rating: Float? = null,
    val reviewerProfilePic: String = ""
)