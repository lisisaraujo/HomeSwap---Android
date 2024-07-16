package com.example.homeswap_android.data.models

data class UserData (
   var userID: String = "",
   val country: String = "",
   val city: String = "",
   val name: String = "",
   val email: String = "",
   val profilePic: String = "",
   val reviews: MutableList<Review> = mutableListOf(),
   var reviewsCount: Int? = 0,
   var rating: Float? = 0F,
   var swaps: Int = 0,
   val bioDescription: String = ""
)