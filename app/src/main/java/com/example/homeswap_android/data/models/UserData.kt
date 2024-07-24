package com.example.homeswap_android.data.models

data class UserData (
   val userID: String? = "",
   val location: String = "",
   val name: String = "",
   val email: String = "",
   var profilePic: String = "",
   val reviews: MutableList<Review> = mutableListOf(),
   var reviewsCount: Int? = 0,
   var rating: Float? = 0F,
   var swaps: Int = 0,
   val bioDescription: String = "",
   val registrationDate: String = ""
)