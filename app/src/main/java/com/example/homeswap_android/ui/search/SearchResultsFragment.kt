package com.example.homeswap_android.ui.search

import ApartmentAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentSearchResultsBinding
import com.example.homeswap_android.viewModels.FiltersViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchResultsFragment : Fragment() {

    val TAG = "SearchResultsFragment"
    private lateinit var binding: FragmentSearchResultsBinding
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()

    private lateinit var apartmentAdapter: ApartmentAdapter
    private val args: SearchResultsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // go to clicked apartment
        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                SearchResultsFragmentDirections.actionSearchResultsFragmentToApartmentDetailsFragment(
                    apartment.apartmentID
                )
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentsViewModel.toggleLike(apartment)
        }

        apartmentAdapter = ApartmentAdapter(
            emptyList(),
            itemClickedCallback,
            onLikeClickListener
        )
        binding.searchResultRV.adapter = apartmentAdapter


        val filters = parseFilters(args.filters!!)

        if (filters.isNotEmpty()) {
            displaySelectedFilters(filters)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                apartmentsViewModel.currentFilters.collectLatest { filters ->
                    updateFilterChips(filters)
                }
            }

            launch {
                apartmentsViewModel.apartmentsBySearch.collectLatest { apartmentsBySearch ->
                    if (apartmentsBySearch.isNotEmpty()) {
                        apartmentAdapter.updateApartments(apartmentsBySearch)
                        updateLoadingState(false)
                        binding.searchResultsInfoTV.text =
                            "Found ${apartmentsBySearch.size} results for your search"
                    }
                }
            }
        }


        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // In SearchResultsFragment

        binding.flightSearchChip.setOnClickListener {

            val bundle = Bundle().apply {
                putString("destination", args.destination)
                putString("departureDate", args.departureDate)
                putString("returnDate", args.returnDate)
            }
            findNavController().navigate(R.id.checkFlightsFragment, bundle)
        }


        apartmentsViewModel.loadingApartments.observe(viewLifecycleOwner) { isLoading ->
            updateLoadingState(isLoading)
        }

    }
    private fun parseFilters(filtersString: String): Map<String, Any?> {
        return filtersString.removeSurrounding("{", "}")
            .split(", ")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0]
                    val value = when {
                        parts[1].startsWith("[") && parts[1].endsWith("]") -> {
                            parts[1].removeSurrounding("[", "]").split(", ").map { it.trim('"') }
                        }
                        parts[1].toIntOrNull() != null -> parts[1].toInt()
                        else -> parts[1]
                    }
                    key to value
                } else {
                    null
                }
            }.toMap()
    }

    private fun displaySelectedFilters(filters: Map<String, Any?>) {
        filters.forEach { (key, value) ->
            when (key) {
                "typeOfHome", "rooms", "maxGuests" , "city" -> addFilterChip(key, value.toString())
                "amenities" -> (value as? List<String>)?.forEach { amenity -> addFilterChip(amenity, amenity) }
            }
        }
    }

    private fun addFilterChip(key: String, value: String) {
        val chip = Chip(requireContext()).apply {
            text = if (key == value) value else "$key: $value"
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                binding.selectedFiltersChipGroup.removeView(this)
                if (key == value) {
                    //this is an amenity
                    apartmentsViewModel.removeAmenity(value)
                } else {
                    apartmentsViewModel.removeFilter(key)
                }
            }
        }
        binding.selectedFiltersChipGroup.addView(chip)
    }

    private fun updateLoadingState(isLoading: Boolean) {
        binding.loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun updateFilterChips(filters: Map<String, Any?>) {
        binding.selectedFiltersChipGroup.removeAllViews()
        filters.forEach { (key, value) ->
            when (key) {
                "typeOfHome", "rooms", "maxGuests" , "city" -> addFilterChip(key, value.toString())
                "amenities" -> (value as? List<String>)?.forEach { amenity -> addFilterChip(amenity, amenity) }
            }
        }
    }
}
