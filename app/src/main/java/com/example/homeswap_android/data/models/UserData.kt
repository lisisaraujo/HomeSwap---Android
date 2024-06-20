package com.example.homeswap_android.data.models

import com.example.homeswap_android.data.models.apiData.Segment

data class UserData (
   var userID: String = "",
   val name: String = "",
   val email: String = "",
   val profilePic: String = "",
   val reviews: List<UserReviewData> = listOf(),
   val apartment: Apartment = Apartment()
)