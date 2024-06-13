package com.example.homeswap_android.data.remote

import com.example.homeswap_android.amadeusAPI.amadeusToken.AmadeusAccessTokenProvider
import com.example.homeswap_android.amadeusAPI.amadeusToken.AccessTokenAuthenticator
import com.example.homeswap_android.amadeusAPI.amadeusToken.AccessTokenInterceptor
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://test.api.amadeus.com/"

val tokenProvider = AmadeusAccessTokenProvider(
    clientId = "5IPT3yNfOTjQbDGvr6awwyGRw0JfmrI4",
    clientSecret = "ncqEFgmjA21mo81c"
)

val authenticator = AccessTokenAuthenticator(tokenProvider)
val interceptor = AccessTokenInterceptor(tokenProvider)

val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val client = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .authenticator(authenticator)
    .addInterceptor(loggingInterceptor)
    .build()

val moshi: Moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface FlightsApiService {
    @GET("v2/shopping/flight-offers")
    suspend fun getFlights(
        @Query("originLocationCode") origin: String,
        @Query("destinationLocationCode") destination: String,
        @Query("departureDate") departureDate: String,
        @Query("adults") adults: Int = 1,
    ): FlightResponse
}

object FlightsApi {
    val flightsApiService: FlightsApiService by lazy { retrofit.create(FlightsApiService::class.java) }
}
