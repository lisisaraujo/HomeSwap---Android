package com.example.homeswap_android.data.models


data class Apartment (
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
    var reviews: MutableList<ApartmentReview> = mutableListOf(),
    var pictures: MutableList<String> = mutableListOf(),
    var liked: Boolean = false,
    var petsAllowed: Boolean = false,
    var rooms: Int = 0,
    var maxGuests: Int = 0,
    var typeOfHome: String = "",
    var homeOffice: Boolean = false,
    var rating: Double = 0.0,
    var hasWifi: Boolean = false
)