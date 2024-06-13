package com.example.homeswap_android.data.models

import java.util.Date

data class UserReviewData(
    val user: UserData,
    val userName: String = "",
    val comment: String = "",
    val rating: Int = 0,
    val profilePicture: String = "",
    val date: String = "",
)