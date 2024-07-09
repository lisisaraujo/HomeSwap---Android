package com.example.homeswap_android.ui.checkFlights

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.data.models.apiData.Dictionaries
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.utils.Utils.dateFormat
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.ParseException
import java.util.Date

class CheckFlightsFragment : Fragment() {

    val TAG = "CheckFlightsFragment"

    private lateinit var binding: FragmentCheckFlightsBinding

    private val flightViewModel: FlightsViewModel by activityViewModels()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

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

    private lateinit var placesClient: PlacesClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        placesClient = Places.createClient(requireContext())

        setupAutoCompleteTextView(binding.etOrigin)
        setupAutoCompleteTextView(binding.etDestination)


        //set up recyclerview
        val recyclerView = binding.rvFlightsList
        val flightAdapter =
            FlightAdapter(emptyList(), Dictionaries(emptyMap(), emptyMap(), emptyMap(), emptyMap()))
        recyclerView.adapter = flightAdapter

        //clear search fields
        flightViewModel.clearSearch.observe(viewLifecycleOwner) { shouldClear ->
            if (shouldClear) {
                clearInputFields()
            }
        }


        //get args from bundle
        argsDestination = arguments?.getString("destination")
        argsDepartureDateString = arguments?.getString("departureDate")
        argsReturnDateString = arguments?.getString("returnDate")

        hasBundleData =
            argsDestination != null && argsDepartureDateString != null && argsReturnDateString != null


        setupDateRangePicker()
        observeUserData()


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
            userOrigin = user?.city
            prefillFromApartmentSearch()

            if (hasBundleData) {
                performSearchWithBundle()
            }
        }
    }

    private fun performSearchWithBundle() {
        var departureDate: Date? = null
        var returnDate: Date? = null

        Log.d(
            "CheckFlightsBundleData",
            "$argsDestination, $argsDepartureDateString, $argsReturnDateString, $userOrigin"
        )

        try {
            departureDate = argsDepartureDateString?.let { dateFormat.parse(it) }
            returnDate = argsReturnDateString?.let { dateFormat.parse(it) }
        } catch (e: ParseException) {
            Log.e("CheckFlightsFragment", "Error parsing dates: ${e.message}")
        }

        if (userOrigin != null && argsDestination != null && departureDate != null && returnDate != null) {
            flightViewModel.searchRoundTripFlights(
                userOrigin!!,
                argsDestination!!,
                departureDate,
                returnDate
            )
        } else {
            Toast.makeText(context, "Insufficient data for search", Toast.LENGTH_LONG).show()
        }
    }

    private fun performManualSearch() {
        flightViewModel.clearFlightsSearch()

        origin = binding.etOrigin.text.toString()
        destination = binding.etDestination.text.toString()
        departureDate = selectedStartDate
        returnDate = selectedEndDate

        if (origin!!.isNotBlank() && destination!!.isNotBlank() && departureDate != null && returnDate != null) {
            flightViewModel.searchRoundTripFlights(origin!!, destination!!, departureDate!!, returnDate!!)
        } else {
            Toast.makeText(context, "Fill out all data to search for flights", Toast.LENGTH_LONG)
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun prefillFromApartmentSearch() {
        binding.etOrigin.setText(userOrigin)
        binding.etDestination.setText(argsDestination)
        binding.etDateRange.setText("$argsDepartureDateString - $argsReturnDateString")
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

    private fun setupAutoCompleteTextView(autoCompleteTextView: AutoCompleteTextView) {
        val adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) >= 2) {
                    performAutoComplete(s.toString(), adapter)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //add this part to handle item selection
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedPlace = adapter.getItem(position)
            //do something with the selected place
            handleSelectedPlace(autoCompleteTextView, selectedPlace)
        }
    }

    private fun handleSelectedPlace(view: AutoCompleteTextView, selectedPlace: String?) {
        selectedPlace?.let {
            //extract the first part of the string before the comma
            val firstPart = it.split(",").firstOrNull()?.trim() ?: it

            //set the text of the AutoCompleteTextView to the first part, because second part is the country
            view.setText(firstPart)

            //depending on which AutoCompleteTextView was clicked, store the first part
            when (view.id) {
                R.id.et_origin -> {
                    origin = firstPart
                }
                R.id.et_destination -> {
                    destination = firstPart
                }
            }
        }
    }

    private fun performAutoComplete(query: String, adapter: ArrayAdapter<String>) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            adapter.clear()
            response.autocompletePredictions.forEach { prediction ->
                adapter.add(prediction.getFullText(null).toString())
            }
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                Log.e(TAG, "Place not found: ${exception.statusCode}")
            }
        }
    }
}
