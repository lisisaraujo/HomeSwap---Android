package com.example.homeswap_android.data.models

import java.time.LocalDateTime

data class ApartmentReview(
    val user: UserData? = UserData(),
    val date: String = "",
    val comment: String? = "",
)