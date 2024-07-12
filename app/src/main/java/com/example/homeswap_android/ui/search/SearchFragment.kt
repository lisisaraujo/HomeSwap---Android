package com.example.homeswap_android.ui.search

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentSearchBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.viewModels.FiltersViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max

class SearchFragment : Fragment() {
    val TAG = "SearchFragment"

    private lateinit var binding: FragmentSearchBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private lateinit var placesClient: PlacesClient

    private var startDate: String? = null
    private var endDate: String? = null
    private var destination: String? = null
    private var filters: Map<String, Any>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        apartmentViewModel.clearSearch()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSliders()
        setupAdditionalFiltersToggle()

        placesClient = Places.createClient(requireContext())

        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.destinationInput,
            placesClient
        ) { selectedPlace ->
            destination = selectedPlace.split(",").firstOrNull()?.trim()
            binding.destinationInput.setText(selectedPlace)
        }

        binding.etDateRange.setOnClickListener {
            Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                startDate = start
                endDate = end
                binding.selectedDateRange.hint = "$start - $end"
            }
        }

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

        binding.searchButton.setOnClickListener {
            collectFilters()
            filters = collectFilters()

            apartmentViewModel.searchApartments(
                city = destination.takeIf { !it.isNullOrBlank() },
                startDate = startDate.takeIf { it.isNullOrEmpty() },
                endDate = endDate.takeIf { it.isNullOrEmpty() },
                typeOfHome = filters!!["typeOfHome"] as? String,
                amenities = (filters!!["amenities"] as? List<String>) ?: emptyList(),
                rooms = (filters!!["rooms"] as? Int),
                maxGuests = (filters!!["maxGuests"] as? Int)
            )
        }

        binding.searchApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.clearSearchButton.setOnClickListener {
            clearSearch()
        }

    }

//    private fun setupTypeOfHomeDropdown() {
//        val items = listOf("Apartment", "House", "Studio", "Villa", "Cottage")
//        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
//        (binding.typeOfHomeAutoComplete as? AutoCompleteTextView)?.setAdapter(adapter)
//    }
//
    private fun setupSliders() {
        binding.roomsSlider.addOnChangeListener { _, value, _ ->
            binding.roomsLabel.text = "Number of Rooms: ${value.toInt()}"
        }

        binding.maxGuestsSlider.addOnChangeListener { _, value, _ ->
            binding.maxGuestsLabel.text = "Max Guests: ${value.toInt()}"
        }
    }
//
    private fun setupAdditionalFiltersToggle() {
        binding.additionalFiltersHeader.setOnClickListener {
            val isVisible = binding.additionalFiltersContent.visibility == View.VISIBLE
            binding.additionalFiltersContent.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.additionalFiltersArrow.setImageResource(
                if (isVisible) R.drawable.baseline_arrow_drop_down_24 else R.drawable.baseline_keyboard_arrow_up_24
            )
        }
    }

    private fun collectFilters(): Map<String, Any> {
        val typeOfHome = binding.typeOfHomeChipGroup.findViewById<Chip>(binding.typeOfHomeChipGroup.checkedChipId)?.text.toString()
        val amenities = binding.amenitiesChipGroup.checkedChipIds.map {
            binding.amenitiesChipGroup.findViewById<Chip>(it).text.toString()
        }

        return mapOf(
            "location" to binding.destinationInput.text.toString(),
            "dateRange" to "${startDate ?: ""} - ${endDate ?: ""}",
            "typeOfHome" to typeOfHome,
            "rooms" to binding.roomsSlider.value.toInt(),
            "maxGuests" to binding.maxGuestsSlider.value.toInt(),
            "amenities" to amenities
        )
    }

//    private fun showFilterBottomSheet() {
//        val filterBottomSheet = FilterBottomSheetFragment.newInstance()
//        filterBottomSheet.show(childFragmentManager, FilterBottomSheetFragment.TAG)
//    }

    private fun clearSearch() {
        // Clear destination input
        binding.destinationInput.text?.clear()

        // Clear date range
        binding.etDateRange.text?.clear()
        binding.selectedDateRange.hint = "Select Dates"
        startDate = null
        endDate = null

        // Uncheck all chips in type of home
        binding.typeOfHomeChipGroup.clearCheck()

        // Reset sliders
        binding.roomsSlider.value = binding.roomsSlider.valueFrom
        binding.maxGuestsSlider.value = binding.maxGuestsSlider.valueFrom

        // Uncheck all amenity chips
        binding.amenitiesChipGroup.clearCheck()

        // Clear search in ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            apartmentViewModel.clearSearch()
        }

        // Optionally, you can show a message to the user
        Toast.makeText(context, "Search cleared", Toast.LENGTH_SHORT).show()
    }
}

