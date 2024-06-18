package com.example.homeswap_android.data.models


data class Apartment (
    val apartmentID: String = "",
    val userID: String = "",
    val title: String = "",
    val country: String = "",
    val city: String = "",
    val address: String = "",
    val availableDates: String = "",
    val reviews: MutableList<ApartmentReview> = mutableListOf(),
    val pictures: MutableList<String> = mutableListOf(),
    val isLiked: Boolean = false
)