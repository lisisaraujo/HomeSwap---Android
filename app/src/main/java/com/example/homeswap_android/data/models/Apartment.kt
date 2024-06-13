package com.example.homeswap_android.data.models

import android.graphics.Picture

data class Apartment (
    val title: String = "",
    val country: String = "",
    val city: String = "",
    val address: String = "",
    val availableDates: String = "",
    val reviews: List<ApartmentReview> = listOf(),
    val pictures: List<Picture> = listOf(),
    val isLiked: Boolean = false
)