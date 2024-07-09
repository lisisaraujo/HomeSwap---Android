package com.example.homeswap_android.ui.checkFlights

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.data.models.apiData.Dictionaries
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CheckFlightsFragment : Fragment() {
    private lateinit var binding: FragmentCheckFlightsBinding

    private val flightViewModel: FlightsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    private var userOrigin: String? = null

    private var destination: String? = null
    private var departureDateString: String? = null
    private var returnDateString: String? = null

    private var isSearchInitiated = false

    private var hasBundleData = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        flightViewModel.clearSearch.observe(viewLifecycleOwner) { shouldClear ->
            if (shouldClear) {
                clearInputFields()
            }
        }
        destination = arguments?.getString("destination")
        departureDateString = arguments?.getString("departureDate")
        returnDateString = arguments?.getString("returnDate")

        hasBundleData = destination != null && departureDateString != null && returnDateString != null

        setupDateRangePicker()
        observeUserData()

        val recyclerView = binding.rvFlightsList
        val flightAdapter = FlightAdapter(emptyList(), Dictionaries(emptyMap(), emptyMap(), emptyMap(), emptyMap()))
        recyclerView.adapter = flightAdapter

        flightViewModel.flightResponse.observe(viewLifecycleOwner) { response ->
            flightAdapter.updateFlights(response.data, response.dictionaries)
            updateLoadingState(false)
        }

        flightViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

        flightViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                if (it.isNotEmpty()) {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            }
            updateLoadingState(false)
        }

        binding.btnSearchFlights.setOnClickListener {
            performManualSearch()
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvFlightsList.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun observeUserData() {
        userViewModel.loggedInUserData.observe(viewLifecycleOwner) { user ->
            Log.d("UserDataLoggedIn", user.toString())
            userOrigin = user?.city
            prefillFromApartmentSearch()

            if (hasBundleData) {
                performSearchWithBundle()
            }
        }
    }

    private fun performSearchWithBundle() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var departureDate: Date? = null
        var returnDate: Date? = null

        Log.d(
            "CheckFlightsBundleData",
            "$destination, $departureDateString, $returnDateString, $userOrigin"
        )

        try {
            departureDate = departureDateString?.let { dateFormat.parse(it) }
            returnDate = returnDateString?.let { dateFormat.parse(it) }
        } catch (e: ParseException) {
            Log.e("CheckFlightsFragment", "Error parsing dates: ${e.message}")
        }

        if (userOrigin != null && destination != null && departureDate != null && returnDate != null) {
            flightViewModel.searchRoundTripFlights(
                userOrigin!!,
                destination!!,
                departureDate,
                returnDate
            )
        } else {
            Toast.makeText(context, "Insufficient data for search", Toast.LENGTH_LONG).show()
        }
    }

    private fun performManualSearch() {
        flightViewModel.clearFlightsSearch()

        val origin = binding.etOrigin.text.toString()
        val destination = binding.etDestination.text.toString()
        val departureDate = selectedStartDate
        val returnDate = selectedEndDate

        if (origin.isNotBlank() && destination.isNotBlank() && departureDate != null && returnDate != null) {
            flightViewModel.searchRoundTripFlights(origin, destination, departureDate, returnDate)
        } else {
            Toast.makeText(context, "Fill out all data to search for flights", Toast.LENGTH_LONG).show()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun prefillFromApartmentSearch() {
        binding.etOrigin.setText(userOrigin)
        binding.etDestination.setText(destination)
        binding.etDateRange.setText("$departureDateString - $returnDateString")
    }

    private fun setupDateRangePicker() {
        binding.etDateRange.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Select Dates")
            .build()

        picker.show(parentFragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            selection?.let {
                selectedStartDate = Date(it.first)
                selectedEndDate = Date(it.second)
                updateDateRangeDisplay()
            }
        }
    }

    private fun updateDateRangeDisplay() {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val startDateString = selectedStartDate?.let { dateFormat.format(it) } ?: "Not selected"
        val endDateString = selectedEndDate?.let { dateFormat.format(it) } ?: "Not selected"
        binding.etDateRange.setText("$startDateString - $endDateString")
        binding.etDateRange.contentDescription =
            "Selected date range: from $startDateString to $endDateString"
    }

    private fun clearInputFields() {
        binding.etOrigin.setText("")
        binding.etDestination.setText("")
        binding.etDateRange.setText("")
        selectedStartDate = null
        selectedEndDate = null
    }
}
