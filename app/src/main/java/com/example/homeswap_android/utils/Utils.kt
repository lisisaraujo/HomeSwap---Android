package com.example.homeswap_android.utils

import com.example.homeswap_android.amadeusAPI.amadeusToken.AmadeusAccessTokenProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    const val amadeusAccessToken = "0VT62ZK1ntvrmEnwUxSGPLm3DNIO"

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val API_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun Date.toApiFormat(): String {
        return API_DATE_FORMAT.format(this)
    }

    val amadeusClientID = "5IPT3yNfOTjQbDGvr6awwyGRw0JfmrI4"
    val amadeusClientSecret = "ncqEFgmjA21mo81c"
    val googlePlacesApiKey = "AIzaSyBSK7IgKTs8wt9_ig1BlGd70gRwzZbNzZA"



}