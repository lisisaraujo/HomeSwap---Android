package com.example.homeswap_android.data.models

import androidx.compose.ui.graphics.drawscope.Stroke

open class Review(
    open val reviewType: String = "",
    open val reviewID: String = "",
    open val reviewerID: String = "",
    open val reviewerName: String = "",
    open val review: String = "",
    open val date: String = "",
    open val rating: Float? = null,
    open val reviewerProfilePic: String = ""
)