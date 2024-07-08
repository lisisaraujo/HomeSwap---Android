package com.example.homeswap_android.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeswap_android.data.models.apiData.FlightOffer
import com.example.homeswap_android.data.models.apiData.FlightResponse
import com.example.homeswap_android.data.models.apiData.Price
import com.example.homeswap_android.data.repositories.FlightsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlightsViewModel : ViewModel() {
    private val TAG = "FlightsViewModel"

    private val repository = FlightsRepository()

    private val _flights = MutableLiveData<List<FlightOffer>>()
    val flights: LiveData<List<FlightOffer>> = _flights

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _flightResponse = MutableLiveData<FlightResponse>()
    val flightResponse: LiveData<FlightResponse> = _flightResponse


    fun loadFlights(origin: String, destination: String, departureDate: Date, adults: Int = 1) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(departureDate)
                val flightsList = repository.loadFlights(origin, destination, formattedDate, adults)
                _flights.postValue(flightsList)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading flights", e)
                _errorMessage.postValue("Failed to load flights: ${e.localizedMessage}")
                _isLoading.postValue(false)
            }
        }
    }

    fun searchFlights(originCity: String, destinationCity: String, departureDate: Date, adults: Int = 1) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(departureDate)
                val response = repository.searchFlightsByCity(originCity, destinationCity, formattedDate, adults)
                _flightResponse.postValue(response)
                _flights.postValue(response.data)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching flights", e)
                _errorMessage.postValue("Failed to search flights: ${e.localizedMessage}")
                _isLoading.postValue(false)
            }
        }
    }

    fun searchRoundTripFlights(originCity: String, destinationCity: String, departureDate: Date, returnDate: Date, adults: Int = 1) {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val formattedDepartureDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(departureDate)
                val formattedReturnDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(returnDate)

                val outboundResponse = repository.searchFlightsByCity(originCity, destinationCity, formattedDepartureDate, adults)
                val inboundResponse = repository.searchFlightsByCity(destinationCity, originCity, formattedReturnDate, adults)

                // Combine the responses
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
                    dictionaries = outboundResponse.dictionaries // Assuming dictionaries are the same for both responses
                )

                _flightResponse.postValue(combinedResponse)
                _flights.postValue(combinedFlights)
                _isLoading.postValue(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error searching round-trip flights", e)
                _errorMessage.postValue("Failed to search round-trip flights: ${e.localizedMessage}")
                _isLoading.postValue(false)
            }
        }
    }

}
