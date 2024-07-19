package com.example.homeswap_android.ui.checkFlights

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import java.util.Date

class CheckFlightsFragment : Fragment() {

    val TAG = "CheckFlightsFragment"

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

    private var isOneWayTrip = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)


        binding.toggleTripType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                isOneWayTrip = checkedId == R.id.btnOneWay
                updateUIForTripType()
            }
        }

        // Set initial UI state
        updateUIForTripType()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //google places API setup
        placesClient = Places.createClient(requireContext())

        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.etOrigin,
            placesClient
        ) { selectedPlace ->
            origin = selectedPlace.split(",").firstOrNull()?.trim()
            binding.etOrigin.setText(selectedPlace)
        }

        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.etDestination,
            placesClient
        ) { selectedPlace ->
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

        Log.d(TAG, initialSearchDone.toString())

        binding.toggleTripType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                isOneWayTrip = checkedId == R.id.btnOneWay
                updateUIForTripType()
            }
        }

        binding.etDateRange.setOnClickListener {
            if (isOneWayTrip) {
                Utils.showDatePicker(parentFragmentManager) { date ->
                    selectedStartDate = Utils.dateFormat.parse(date)
                    selectedEndDate = null
                    updateDateDisplay()
                }
            } else {
                Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                    selectedStartDate = Utils.dateFormat.parse(start)
                    selectedEndDate = Utils.dateFormat.parse(end)
                    updateDateDisplay()
                }
            }
        }

        binding.btnSearchFlights.setOnClickListener {
            performSearch()
        }


        observeViewModels()
        handleBundleArgs()
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            userViewModel.loggedInUserData.collect { user ->
                userOrigin = user?.city
                if (hasBundleData && !initialSearchDone) {
                    prefillFromApartmentSearch()
                    performSearchWithBundle()
                    initialSearchDone = true
                }
            }
        }

        flightViewModel.flightResponse.observe(viewLifecycleOwner) { response ->
            Log.d(TAG, "Received flight response: ${response?.data?.size} flights")
            updateLoadingState(false)
            if (response != null && response.data.isNotEmpty()) {
                val adapter = binding.rvFlightsList.adapter as? FlightAdapter
                if (adapter == null) {
                    Log.d(TAG, "Creating new adapter")
                    binding.rvFlightsList.adapter =
                        FlightAdapter(response.data, response.dictionaries)
                } else {
                    Log.d(TAG, "Updating existing adapter")
                    adapter.updateFlights(response.data, response.dictionaries)
                }
                binding.rvFlightsList.visibility = View.VISIBLE
                binding.noResultsTV.visibility = View.GONE
            } else {
                Log.d(TAG, "No flights found or null response")
                binding.rvFlightsList.visibility = View.GONE
                binding.noResultsTV.visibility = View.VISIBLE
                binding.noResultsTV.text = "No flights found"
            }
        }


        flightViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
            if (isLoading) {
                binding.rvFlightsList.visibility = View.GONE
                binding.noResultsTV.visibility = View.GONE
            }
        }

        flightViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e(TAG, "Error received: $it")
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
        binding.etDateRange.setText(
            getString(
                R.string.date_range,
                argsDepartureDateString,
                argsReturnDateString
            )
        )

        selectedStartDate = departureDate
        selectedEndDate = returnDate

        //set to round trip for bundle search
        isOneWayTrip = false
        binding.toggleTripType.check(R.id.btnRoundTrip)
        updateUIForTripType()

        flightViewModel.clearSearch()
        performSearch()
    }


    private fun updateUIForTripType() {
        if (isOneWayTrip) {
            binding.toggleTripType.check(R.id.btnOneWay)
            binding.etDateRange.hint = getString(R.string.departure_date)
            selectedEndDate = null
        } else {
            binding.toggleTripType.check(R.id.btnRoundTrip)
            binding.etDateRange.hint = getString(R.string.select_dates)
        }
        updateDateRangeDisplay()
    }

    private fun updateDateDisplay() {
        if (isOneWayTrip) {
            val formattedStartDate = selectedStartDate?.let { Utils.dateFormat.format(it) }
            binding.etDateRange.setText(formattedStartDate)
        } else {
            val formattedStartDate = selectedStartDate?.let { Utils.dateFormat.format(it) }
            val formattedEndDate = selectedEndDate?.let { Utils.dateFormat.format(it) }
            binding.etDateRange.setText(
                getString(
                    R.string.date_range,
                    formattedStartDate,
                    formattedEndDate
                )
            )
        }
    }

    private fun performSearch() {
        if (origin.isNullOrEmpty() || destination.isNullOrEmpty() || selectedStartDate == null) {
            showError("Please fill in all the fields.")
            return
        }

        //check if the selected start date is in the past
        if (selectedStartDate!!.before(Date())) {
            showError("The selected start date is invalid. Please select a future date.")
            return
        }

        // Clear existing flights
        flightViewModel.clearSearch()

        // Hide RV and "No results" TextView
        binding.rvFlightsList.visibility = View.GONE
        binding.noResultsTV.visibility = View.GONE

        if (isOneWayTrip) {
            Log.d(TAG, "Performing one-way search")
            flightViewModel.searchOneWayFlights(
                originCity = origin!!,
                destinationCity = destination!!,
                departureDate = selectedStartDate!!
            )
        } else {
            if (selectedEndDate == null) {
                showError("Please select a return date for round-trip.")
                return
            }
            Log.d(TAG, "Performing round-trip search")
            flightViewModel.searchRoundTripFlights(
                originCity = origin!!,
                destinationCity = destination!!,
                departureDate = selectedStartDate!!,
                returnDate = selectedEndDate!!
            )
        }
    }


    private fun updateDateRangeDisplay() {
        when {
            selectedStartDate != null && selectedEndDate != null -> {
                val formattedStartDate = Utils.dateFormat.format(selectedStartDate!!)
                val formattedEndDate = Utils.dateFormat.format(selectedEndDate!!)
                binding.etDateRange.setText(
                    getString(
                        R.string.date_range,
                        formattedStartDate,
                        formattedEndDate
                    )
                )
            }

            selectedStartDate != null -> {
                val formattedStartDate = Utils.dateFormat.format(selectedStartDate!!)
                if (isOneWayTrip) {
                    binding.etDateRange.setText(formattedStartDate)
                } else {
                    binding.etDateRange.setText(
                        getString(
                            R.string.date_range_start_only,
                            formattedStartDate
                        )
                    )
                }
            }

            else -> {
                binding.etDateRange.setText("")
            }
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


}

