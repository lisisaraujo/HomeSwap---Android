package com.example.homeswap_android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.homeswap_android.R
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object Utils {

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
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        val constraintsBuilder = CalendarConstraints.Builder()
            .setStart(today)
            .setValidator(DateValidatorPointForward.now())

        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select Dates")
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.ThemeMaterialCalendar)
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .build()

        picker.show(fragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            val startDate = dateFormat.format(Date(selection.first))
            val endDate = dateFormat.format(Date(selection.second))
            onDateSelected(startDate, endDate)
        }
    }



    fun showDatePicker(fragmentManager: FragmentManager, onDateSelected: (String) -> Unit) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        // Create a CalendarConstraints.Builder to set the minimum date
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now()) // Disable past dates

        val builder = MaterialDatePicker.Builder.datePicker()
            .setSelection(today) //set today's date as the default selection
            .setCalendarConstraints(constraintsBuilder.build())
            .setTheme(R.style.ThemeMaterialCalendar)


        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selectedDate ->
            val formattedDate = dateFormat.format(Date(selectedDate))
            onDateSelected(formattedDate)
        }
        picker.show(fragmentManager, picker.toString())
    }


     fun updateLikeButton(button: MaterialButton, liked: Boolean) {
        val iconRes = if (liked) R.drawable.baseline_favorite_24 else R.drawable.favorite_48px
        button.setIconResource(iconRes)

        val backgroundTintList = if (liked) {
            ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.liked_background_color))
        } else {
            ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.unliked_background_color))
        }
        button.backgroundTintList = backgroundTintList

        val iconTint = if (liked) {
            ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.liked_icon_color))
        } else {
            ColorStateList.valueOf(ContextCompat.getColor(button.context, R.color.unliked_icon_color))
        }
        button.iconTint = iconTint
    }

 fun showLoadingOverlay(loadingOverlay: ConstraintLayout) {
        loadingOverlay.visibility = View.VISIBLE
    }

fun hideLoadingOverlay(loadingOverlay: ConstraintLayout) {
        loadingOverlay.visibility = View.GONE
    }
}