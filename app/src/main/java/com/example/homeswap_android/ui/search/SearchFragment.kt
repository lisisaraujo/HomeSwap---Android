package com.example.homeswap_android.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSearchBinding
import com.example.homeswap_android.utils.Utils.dateFormat
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date

class SearchFragment : Fragment() {

    val TAG = "SearchFragment"

    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()

    private lateinit var placesClient: PlacesClient

    var startDate: String? = null
    var endDate: String? = null
    var country: String? = null
    var destination: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        placesClient = Places.createClient(requireContext())

        setupAutoCompleteTextView(binding.cityInput)
        setupAutoCompleteTextView(binding.countryInput)

        binding.showDatePickerButton.setOnClickListener {
            showDateRangePicker()
        }

        apartmentViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearchList ->
            Log.d("ApartmentsBySearch", apartmentsBySearchList.toString())
            Log.d("SearchFragment", "$startDate, $endDate, $destination")
            if (apartmentsBySearchList.isNotEmpty()) {
                findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToSearchResultsFragment(
                        destination = destination,
                        departureDate = startDate,
                        returnDate = endDate
                    )
                )
            } else {
                Toast.makeText(context, "No apartments found", Toast.LENGTH_LONG).show()
            }
        }

        binding.searchButton.setOnClickListener {

            country = binding.countryInput.text.toString().trim()
            destination = binding.cityInput.text.toString().trim()

            Log.d("SearchFragmentStartDate", "$startDate, $endDate")
            Log.d("SearchFragmentDestinationCountry", "$destination, $country")
            apartmentViewModel.searchApartments(
                city = destination.takeIf { it!!.isNotBlank() },
                country = country.takeIf { it!!.isNotBlank() },
                startDate = startDate,
                endDate = endDate
            )

        }

        binding.searchApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Select Dates")
            .build()

        picker.show(parentFragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->

            startDate = dateFormat.format(Date(selection.first))
            endDate = dateFormat.format(Date(selection.second))
            binding.selectedDateRange.text =
                "$startDate - $endDate"
        }


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
                R.id.et_destination -> {
                    destination = firstPart
                }
                R.id.et_country -> {
                    country = firstPart
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
