package com.example.homeswap_android.data.models

data class UserData (
   var userID: String = "",
   val name: String = "",
   val email: String = "",
   val profilePic: String = "",
   val reviews: MutableList<Review> = mutableListOf(),
   var swaps: Int = 0
)