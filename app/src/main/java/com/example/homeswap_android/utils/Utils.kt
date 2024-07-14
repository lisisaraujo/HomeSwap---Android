package com.example.homeswap_android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.FragmentManager
import com.example.homeswap_android.R
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utils {

    val amadeusClientID = "5IPT3yNfOTjQbDGvr6awwyGRw0JfmrI4"
    val amadeusClientSecret = "ncqEFgmjA21mo81c"
    val googlePlacesApiKey = "AIzaSyBSK7IgKTs8wt9_ig1BlGd70gRwzZbNzZA"

    @SuppressLint("ConstantLocale")
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    @SuppressLint("ConstantLocale")
    val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fun Date.toApiFormat(): String = apiDateFormat.format(this)

    fun setupAutoCompleteTextView(
        context: Context,
        autoCompleteTextView: AutoCompleteTextView,
        placesClient: PlacesClient,
        onPlaceSelected: (String) -> Unit
    ) {
        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line)
        autoCompleteTextView.setAdapter(adapter)

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) >= 2) {
                    performAutoComplete(s.toString(), adapter, placesClient)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selectedPlace = adapter.getItem(position)
            onPlaceSelected(selectedPlace ?: "")
        }
    }

    private fun performAutoComplete(query: String, adapter: ArrayAdapter<String>, placesClient: PlacesClient) {
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
                Log.e("AutoComplete", "Place not found: ${exception.statusCode}")
            }
        }
    }

    fun showDateRangePicker(
        fragmentManager: FragmentManager,
        onDateSelected: (String, String) -> Unit
    ) {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTheme(R.style.ThemeMaterialCalendar)
            .setTitleText("Select Dates")
            .build()

        picker.show(fragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            val startDate = dateFormat.format(Date(selection.first))
            val endDate = dateFormat.format(Date(selection.second))
            onDateSelected(startDate, endDate)
        }
    }

    fun showDatePicker(fragmentManager: FragmentManager, onDateSelected: (String) -> Unit) {
        val startDate = MaterialDatePicker.todayInUtcMilliseconds()
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .setSelection(startDate)

        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selectedDate ->
            val formattedDate = dateFormat.format(Date(selectedDate))
            onDateSelected(formattedDate)
        }
        picker.show(fragmentManager, picker.toString())
    }
}