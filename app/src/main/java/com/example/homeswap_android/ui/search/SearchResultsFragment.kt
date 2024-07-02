package com.example.homeswap_android.ui.search

import ApartmentAdapter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentSearchResultsBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel

class SearchResultsFragment : Fragment() {
    private lateinit var binding: FragmentSearchResultsBinding
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private lateinit var apartmentAdapter: ApartmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apartmentsViewModel.getApartments()

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            Log.d("ItemCallbackApartment", apartment.apartmentID)
            findNavController().navigate(SearchResultsFragmentDirections.actionSearchResultsFragmentToApartmentDetailsFragment(apartment.apartmentID))
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            Log.d("LikeClicked", "onLikeClickListener called for apartment ${apartment.apartmentID}")
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
            if(!apartmentsBySearch.isNullOrEmpty()) {
                apartmentAdapter.updateApartments(apartmentsBySearch)
                apartmentsViewModel.clearSearch()
            }
        }

        binding.searchResultsBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}