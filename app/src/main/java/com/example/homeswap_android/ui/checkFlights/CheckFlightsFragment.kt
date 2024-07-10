package com.example.homeswap_android.ui.checkFlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Date

class CheckFlightsFragment : Fragment() {

    private lateinit var binding: FragmentCheckFlightsBinding
    private val flightViewModel: FlightsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null
    private var userOrigin: String? = null
    private var argsDestination: String? = null
    private var argsDepartureDateString: String? = null
    private var argsReturnDateString: String? = null
    private var origin: String? = null
    private var destination: String? = null
    private var departureDate: Date? = null
    private var returnDate: Date? = null
    private var hasBundleData = false
    private var initialSearchDone = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // google places API setup
        placesClient = Places.createClient(requireContext())

        Utils.setupAutoCompleteTextView(requireContext(), binding.etOrigin, placesClient) { selectedPlace ->
            origin = selectedPlace.split(",").firstOrNull()?.trim()
            binding.etOrigin.setText(selectedPlace)
        }

        Utils.setupAutoCompleteTextView(requireContext(), binding.etDestination, placesClient) { selectedPlace ->
            destination = selectedPlace.split(",").firstOrNull()?.trim()
            binding.etDestination.setText(selectedPlace)
        }

        binding.etDateRange.setOnClickListener {
            Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                selectedStartDate = Utils.dateFormat.parse(start)
                selectedEndDate = Utils.dateFormat.parse(end)
                updateDateRangeDisplay()
            }
        }

        binding.btnSearchFlights.setOnClickListener {
            performManualSearch()
        }

        observeViewModels()
        handleBundleArgs()
    }

    private fun observeViewModels() {
        //observe the data from logged in user to set search origin based on users location when navigating with bundle
        userViewModel.loggedInUserData.observe(viewLifecycleOwner) { user ->
            userOrigin = user?.city
            prefillFromApartmentSearch()
            if (hasBundleData && !initialSearchDone) {
                performSearchWithBundle()
                initialSearchDone = true
            }
        }

        flightViewModel.flightResponse.observe(viewLifecycleOwner) { response ->
            binding.rvFlightsList.adapter = FlightAdapter(response.data, response.dictionaries)
            updateLoadingState(false)
        }

        flightViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

        flightViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showError(it)
            }
        }
    }

    private fun prefillFromApartmentSearch() {
        binding.etOrigin.setText(userOrigin)
    }

    private fun handleBundleArgs() {
        arguments?.let { bundle ->
            argsDestination = bundle.getString("destination")
            argsDepartureDateString = bundle.getString("departureDate")
            argsReturnDateString = bundle.getString("returnDate")

            if (!argsDestination.isNullOrEmpty() && !argsDepartureDateString.isNullOrEmpty() && !argsReturnDateString.isNullOrEmpty()) {
                hasBundleData = true
            }
        }
    }

    private fun performSearchWithBundle() {
        destination = argsDestination
        departureDate = argsDepartureDateString?.let { Utils.dateFormat.parse(it) }
        returnDate = argsReturnDateString?.let { Utils.dateFormat.parse(it) }
        origin = userOrigin

        binding.etDestination.setText(destination)
        binding.etDateRange.setText(getString(R.string.date_range, argsDepartureDateString, argsReturnDateString))

        // clear previous search data
        flightViewModel.clearSearch()
        performFlightSearch()
    }

    private fun performManualSearch() {
        if (origin.isNullOrEmpty() || destination.isNullOrEmpty() || selectedStartDate == null || selectedEndDate == null) {
            showError("Please fill in all the fields.")
            return
        }

        departureDate = selectedStartDate
        returnDate = selectedEndDate

        flightViewModel.clearSearch()
        performFlightSearch()
    }

    private fun performFlightSearch() {
        flightViewModel.searchRoundTripFlights(
            originCity = origin!!,
            destinationCity = destination!!,
            departureDate = departureDate!!,
            returnDate = returnDate!!
        )
    }

    private fun updateDateRangeDisplay() {
        val formattedStartDate = selectedStartDate?.let { Utils.dateFormat.format(it) }
        val formattedEndDate = selectedEndDate?.let { Utils.dateFormat.format(it) }
        binding.etDateRange.setText(getString(R.string.date_range, formattedStartDate, formattedEndDate))
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

