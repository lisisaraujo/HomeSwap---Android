package com.example.homeswap_android.data.models

import com.example.homeswap_android.data.models.apiData.Segment

data class UserData (
   var userID: String = "",
   val name: String = "",
   val email: String = "",
   val profilePic: String = "",
   val reviews: MutableList<UserReviewData> = mutableListOf(),
   var swaps: Int = 0
)