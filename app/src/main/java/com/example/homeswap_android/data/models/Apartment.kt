package com.example.homeswap_android.data.models


data class Apartment(
    var apartmentID: String = "",
    val userID: String = "",
    val title: String = "",
    val city: String = "",
    val cityLower: String = "",
    val address: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var reviews: MutableList<Review> = mutableListOf(),
    var reviewsCount: Int? = 0,
    var rating: Float? = 0F,
    var coverPicture: String = "",
    var liked: Boolean = false,
    var petsAllowed: Boolean = false,
    var rooms: Int? = null,
    var maxGuests: Int? = null,
    var typeOfHome: String = "",
    var homeOffice: Boolean = false,
    var hasWifi: Boolean = false,
    val registrationDate: String = "",
    val description: String? = ""

)