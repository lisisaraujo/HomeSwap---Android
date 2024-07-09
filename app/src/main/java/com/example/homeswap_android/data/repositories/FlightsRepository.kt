package com.example.homeswap_android.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.homeswap_android.data.models.apiData.Dictionaries
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.data.models.apiData.Price
import com.example.homeswap_android.data.remote.FlightsApi
import com.example.homeswap_android.utils.Utils.dateFormat
import java.util.Date

class FlightsRepository {
    private val TAG = "FlightsRepository"

    private val _flights = MutableLiveData<List<FlightOffer>>()
    val flights: LiveData<List<FlightOffer>> = _flights

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _flightResponse = MutableLiveData<FlightResponse>()
    val flightResponse: LiveData<FlightResponse> = _flightResponse


    private val _clearSearch = MutableLiveData<Boolean>(false)
    val clearSearch: LiveData<Boolean> = _clearSearch

    private val apiService = FlightsApi.flightsApiService

    suspend fun loadFlights(
        origin: String,
        destination: String,
        departureDate: String,
        adults: Int = 1
    ): List<FlightOffer> {
        return try {
            val flightsResponse = apiService.getFlights(
                origin = origin,
                destination = destination,
                departureDate = departureDate,
                adults = adults
            )
            Log.d(TAG, "Flights loaded successfully: $flightsResponse")
            _flights.postValue(flightsResponse.data)
            flightsResponse.data
        } catch (e: Exception) {
            Log.e(TAG, "Error loading flights", e)
            throw e
        }
    }

    suspend fun searchFlightsByCity(originCity: String, destinationCity: String, departureDate: String, adults: Int = 1): FlightResponse {
        val originIata = getIataCode(originCity)
        val destinationIata = getIataCode(destinationCity)

        return apiService.getFlights(originIata, destinationIata, departureDate, adults)
    }

    private suspend fun getIataCode(cityName: String): String {
        val response = apiService.searchAirports(keyword = cityName)
        return response.data.firstOrNull()?.iataCode
            ?: throw IllegalArgumentException("No IATA code found for $cityName")
    }

    suspend fun searchRoundTripFlights(originCity: String, destinationCity: String, departureDate: Date, returnDate: Date, adults: Int = 1) {
        try {
            _isLoading.postValue(true)
            val formattedDepartureDate = dateFormat.format(departureDate)
            val formattedReturnDate = dateFormat.format(returnDate)

            val outboundResponse = searchFlightsByCity(originCity, destinationCity, formattedDepartureDate, adults)
            val inboundResponse = searchFlightsByCity(destinationCity, originCity, formattedReturnDate, adults)

            val combinedFlights = outboundResponse.data.zip(inboundResponse.data) { outbound, inbound ->
                FlightOffer(
                    type = "round-trip",
                    id = "${outbound.id}-${inbound.id}",
                    oneWay = false,
                    numberOfBookableSeats = minOf(outbound.numberOfBookableSeats, inbound.numberOfBookableSeats),
                    itineraries = listOf(outbound.itineraries[0], inbound.itineraries[0]),
                    price = Price(
                        currency = outbound.price.currency,
                        total = (outbound.price.total.toDouble() + inbound.price.total.toDouble()).toString(),
                        base = (outbound.price.base.toDouble() + inbound.price.base.toDouble()).toString(),
                        fees = outbound.price.fees + inbound.price.fees,
                        grandTotal = (outbound.price.grandTotal.toDouble() + inbound.price.grandTotal.toDouble()).toString()
                    )
                )
            }

            val combinedResponse = FlightResponse(
                data = combinedFlights,
                dictionaries = outboundResponse.dictionaries
            )

            _flightResponse.postValue(combinedResponse)
            _flights.postValue(combinedFlights)
        } catch (e: Exception) {
            Log.e(TAG, "Error searching round-trip flights", e)
            _errorMessage.postValue("Failed to search round-trip flights: ${e.localizedMessage}")
        } finally {
            _isLoading.postValue(false)
        }
    }

    fun clearFlightsSearch() {
        _flights.postValue(emptyList())
        _flightResponse.postValue(FlightResponse(
            data = emptyList(),
            dictionaries = Dictionaries(
                carriers = emptyMap(),
                aircraft = emptyMap(),
                currencies = emptyMap(),
                locations = emptyMap()
            )
        ))
        _isLoading.postValue(false)
        _errorMessage.postValue(null)
        _clearSearch.postValue(true)
    }
}
