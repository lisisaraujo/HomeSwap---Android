package com.example.homeswap_android.data.models.apiData

data class FlightResponse(
    val data: List<FlightOffer>,
)

data class FlightOffer(
    val type: String,
    val id: String,
    val oneWay: Boolean,
    val numberOfBookableSeats: Int,
    val itineraries: List<Itinerary>,
    val price: Price,
)

data class Itinerary(
    val duration: String,
    val segments: List<Segment>
)

data class Segment(
    val departure: Departure,
    val arrival: Arrival,
    val number: String,
    val duration: String,
    val id: String,
    val numberOfStops: Int,
)

data class Departure(
    val iataCode: String,
    val terminal: String?,
    val at: String
)

data class Arrival(
    val iataCode: String,
    val terminal: String?,
    val at: String
)

data class Price(
    val currency: String,
    val total: String,
    val base: String,
    val fees: List<Fee>,
    val grandTotal: String
)

data class Fee(
    val amount: String,
    val type: String
)



