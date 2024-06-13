package com.example.homeswap_android.data.models

data class UserData (
   val name: String = "",
   val email: String = "",
   val profilePic: String = "",
   val reviews: List<UserReviewData> = listOf(),
   val apartment: Apartment = Apartment()
)