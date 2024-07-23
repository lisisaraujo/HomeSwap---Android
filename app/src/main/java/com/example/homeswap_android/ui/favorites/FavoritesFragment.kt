package com.example.homeswap_android.ui.favorites

import ApartmentAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentFravoritesBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel


class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFravoritesBinding
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private lateinit var apartmentAdapter: ApartmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFravoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apartmentsViewModel.loadLikedApartments()

        val itemClickedCallback: (Apartment) -> Unit = { apartment ->
            findNavController().navigate(
                FavoritesFragmentDirections.actionFavoritesFragmentToApartmentDetailsFragment(
                    apartment.apartmentID
                )
            )
        }

        val onLikeClickListener: (Apartment) -> Unit = { apartment ->
            apartmentsViewModel.toggleLike(apartment)
            apartmentsViewModel.loadLikedApartments()
        }

        apartmentAdapter = ApartmentAdapter(itemClickedCallback, onLikeClickListener)
        binding.favoriteApartmentsRV.adapter = apartmentAdapter

        apartmentsViewModel.likedApartments.observe(viewLifecycleOwner) { likedApartments ->
            Log.d("FavoritesFragment", "Received ${likedApartments.size} liked apartments")
            apartmentAdapter.submitList(likedApartments)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

}