package com.example.homeswap_android.data.models.apiData

data class FlightResponse(
    val data: List<FlightOffer>,
    val dictionaries: Dictionaries
)

data class Dictionaries(
    val carriers: Map<String, String>,
    val aircraft: Map<String, String>,
    val currencies: Map<String, String>,
    val locations: Map<String, Location>
)
data class Location(
    val cityCode: String,
    val countryCode: String
)


data class FlightOffer(
    val type: String,
    val id: String,
    val oneWay: Boolean,
    val numberOfBookableSeats: Int,
    val itineraries: List<Itinerary>,
    val price: Price,
    )

data class FlightDictionaries(
    val carriers: Map<String, String>
)

data class Itinerary(
    val duration: String,
    val segments: List<Segment>
)

data class Segment(
    val departure: Departure,
    val arrival: Arrival,
    val carrierCode: String,
    val number: String,
    val aircraft: Aircraft,
    val operating: Operating?,
    val duration: String,
    val id: String,
    val numberOfStops: Int
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

// flight search response

data class AirportSearchResponse(
    val data: List<Airport>
)

data class Airport(
    val id: String,
    val type: String,
    val subType: String,
    val name: String,
    val detailedName: String,
    val iataCode: String,
    val address: Address
)

data class Address(
    val cityName: String,
    val countryName: String
)

data class Aircraft(
    val code: String
)

data class Operating(
    val carrierCode: String
)

