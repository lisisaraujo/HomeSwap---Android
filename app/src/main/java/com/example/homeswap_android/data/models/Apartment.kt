package com.example.homeswap_android.data.models


data class Apartment (
    var apartmentID: String = "",
    val userID: String = "",
    val title: String = "",
    val country: String = "",
    val countryLower: String = "",
    val city: String = "",
    val cityLower: String = "",
    val address: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val reviews: MutableList<ApartmentReview> = mutableListOf(),
    val pictures: MutableList<String> = mutableListOf(),
    var liked: Boolean = false
)