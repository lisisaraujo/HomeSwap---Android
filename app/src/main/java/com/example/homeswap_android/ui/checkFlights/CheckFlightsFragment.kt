package com.example.homeswap_android.ui.checkFlights

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.homeswap_android.adapter.FlightAdapter
import com.example.homeswap_android.databinding.FragmentCheckFlightsBinding
import com.example.homeswap_android.viewModels.BottomNavViewModel
import com.example.homeswap_android.viewModels.FlightsViewModel

class CheckFlightsFragment : Fragment() {
    private val viewModel: FlightsViewModel by activityViewModels()
    val viewmodelBottomNav: BottomNavViewModel by activityViewModels()
    private lateinit var binding: FragmentCheckFlightsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckFlightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewmodelBottomNav.showBottomNavBar()
        viewModel.loadFlights()

        val recyclerView = binding.rvFlightsList
        viewModel.flights.observe(viewLifecycleOwner) { flightsList ->
            recyclerView.adapter = FlightAdapter(flightsList)
        }
    }
}
