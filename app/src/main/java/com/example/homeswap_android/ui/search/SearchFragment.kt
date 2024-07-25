package com.example.homeswap_android.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSearchBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    val TAG = "SearchFragment"

    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient

    private var startDate: String? = null
    private var endDate: String? = null
    private var destination: String? = null
    private var filters: MutableMap<String, Any?> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)


        viewLifecycleOwner.lifecycleScope.launch {
            apartmentViewModel.clearSearch()
        }


        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSliders()
        setupAdditionalFiltersToggle()
        setupPlacesAutocomplete()
        setupDateRangePicker()
        setupSearchButton()
        setupClearSearchButton()
        observeSearchCompletion()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPlacesAutocomplete() {
        placesClient = Places.createClient(requireContext())
        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.searchLocationET,
            placesClient
        ) { selectedPlace ->
            updateDestination()
        }

        binding.searchLocationET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateDestination()
            }
        })
    }


    // make my destination be whatever is in my textfield, regardless if it was the selected destination or just the users input
    private fun updateDestination() {
        destination = binding.searchLocationET.text.toString().split(",").firstOrNull()?.lowercase()?.trim() ?: ""
        Log.d(TAG, "Destination updated: $destination")
    }



    @SuppressLint("SetTextI18n")
    private fun setupDateRangePicker() {
        binding.etDateRange.setOnClickListener {
            Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                startDate = start
                endDate = end
                binding.etDateRange.setText("$start - $end")
            }
        }
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            updateFilters()
            viewLifecycleOwner.lifecycleScope.launch {
                apartmentViewModel.searchApartments(filters)
            }
        }
    }

    private fun setupClearSearchButton() {
        binding.clearSearchButton.setOnClickListener {
            clearSearch()
        }
    }

    private fun observeSearchCompletion() {
        viewLifecycleOwner.lifecycleScope.launch {
            apartmentViewModel.searchCompletedEvent.collectLatest {
                findNavController().navigate(
                    SearchFragmentDirections.actionSearchFragmentToSearchResultsFragment(
                        destination = destination,
                        departureDate = startDate,
                        returnDate = endDate,
                        filters = filters.toString()
                    )
                )
            }
        }
    }

    private fun setupSliders() {
        binding.roomsSlider.addOnChangeListener { _, value, _ ->
            binding.roomsLabel.text = "Number of Rooms: ${value.toInt()}"
        }

        binding.maxGuestsSlider.addOnChangeListener { _, value, _ ->
            binding.maxGuestsLabel.text = "Max Guests: ${value.toInt()}"
        }
    }

    private fun setupAdditionalFiltersToggle() {
        binding.additionalFiltersHeader.setOnClickListener {
            val isVisible = binding.additionalFiltersContent.visibility == View.VISIBLE
            binding.additionalFiltersContent.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.additionalFiltersArrow.setImageResource(
                if (isVisible) R.drawable.baseline_arrow_drop_down_24 else R.drawable.baseline_keyboard_arrow_up_24
            )
        }
    }

    private fun updateFilters() {
        filters["city"] = destination?.takeIf { it.isNotBlank() }?.lowercase()?.trim()
        filters["startDate"] = startDate?.takeIf { it.isNotEmpty() }
        filters["endDate"] = endDate?.takeIf { it.isNotEmpty() }

        val selectedTypeOfHome = binding.typeOfHomeChipGroup.findViewById<Chip>(binding.typeOfHomeChipGroup.checkedChipId)?.text?.toString()
        if (!selectedTypeOfHome.isNullOrBlank()) {
            filters["typeOfHome"] = selectedTypeOfHome
        } else {
            filters.remove("typeOfHome")
        }

        val amenities = binding.amenitiesChipGroup.checkedChipIds.map {
            binding.amenitiesChipGroup.findViewById<Chip>(it).text.toString()
        }
        if (amenities.isNotEmpty()) {
            filters["amenities"] = amenities
        } else {
            filters.remove("amenities")
        }

        if (binding.roomsSlider.value > binding.roomsSlider.valueFrom) {
            filters["rooms"] = binding.roomsSlider.value.toInt()
        } else {
            filters.remove("rooms")
        }

        if (binding.maxGuestsSlider.value > binding.maxGuestsSlider.valueFrom) {
            filters["maxGuests"] = binding.maxGuestsSlider.value.toInt()
        } else {
            filters.remove("maxGuests")
        }

        Log.d(TAG, "Filters updated: $filters")
    }


    private fun clearSearch() {
        binding.searchLocationET.text?.clear()
        binding.etDateRange.text?.clear()
        startDate = null
        endDate = null
        binding.typeOfHomeChipGroup.clearCheck()
        binding.roomsSlider.value = binding.roomsSlider.valueFrom
        binding.maxGuestsSlider.value = binding.maxGuestsSlider.valueFrom
        binding.amenitiesChipGroup.clearCheck()

        filters.clear()
        viewLifecycleOwner.lifecycleScope.launch {
            apartmentViewModel.clearSearch()
        }


        Toast.makeText(context, "Search cleared", Toast.LENGTH_SHORT).show()
    }
}

