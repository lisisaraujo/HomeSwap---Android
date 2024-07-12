package com.example.homeswap_android.data.models


data class Apartment(
    var apartmentID: String = "",
    val userID: String = "",
    val title: String = "",
    val country: String = "",
    val countryLower: String = "",
    val city: String = "",
    val cityLower: String = "",
    val address: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var reviews: MutableList<Review> = mutableListOf(),
    var coverPicture: String = "",
    var liked: Boolean = false,
    var petsAllowed: Boolean = false,
    var rooms: Int? = null,
    var maxGuests: Int? = null,
    var typeOfHome: String = "",
    var homeOffice: Boolean = false,
    var rating: Double? = null,
    var hasWifi: Boolean = false
)