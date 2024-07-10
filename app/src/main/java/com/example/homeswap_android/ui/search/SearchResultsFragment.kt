package com.example.homeswap_android.ui.search

import ApartmentAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentSearchResultsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel

class SearchResultsFragment : Fragment() {
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

        // Create a bundle
        val bundle = Bundle()
        bundle.putString("destination", args.destination)
        bundle.putString("departureDate", args.departureDate)
        bundle.putString("returnDate", args.returnDate)

        apartmentsViewModel.getApartments()

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

        apartmentsViewModel.apartmentsBySearch.observe(viewLifecycleOwner) { apartmentsBySearch ->
            Log.d("apartmentsBySearch", apartmentsBySearch.toString())
            if (!apartmentsBySearch.isNullOrEmpty()) {
                apartmentAdapter.updateApartments(apartmentsBySearch)
                binding.searchResultsInfoTV.text =
                    "Found ${apartmentsBySearch.size} results for your search"
                apartmentsViewModel.clearSearch()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.flightSearchChip.setOnClickListener {
            findNavController().navigate(R.id.checkFlightsFragment, bundle)
        }

    }
}
