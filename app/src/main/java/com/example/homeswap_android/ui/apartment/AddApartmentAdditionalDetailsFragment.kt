package com.example.homeswap_android.ui.apartment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentAddApartmentAdditionalDetailsBinding
import com.example.homeswap_android.viewModels.AddApartmentViewModel
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class AddApartmentAdditionalDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAddApartmentAdditionalDetailsBinding
    private val addApartmentViewModel: AddApartmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApartmentAdditionalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeOfHomeDropdown()
        setupSliders()


        addApartmentViewModel.newAddedApartment.observe(viewLifecycleOwner) { apartment ->

            apartment.let {
            binding.submitApartmentBTN.setOnClickListener {
                val rooms = binding.roomsSlider.value.toInt()
                val maxGuests = binding.maxGuestsSlider.value.toInt()
                val typeOfHome = binding.typeOfHomeAutoComplete.text.toString()
                val petsAllowed = binding.petsAllowedSwitch.isChecked
                val homeOffice = binding.homeOfficeSwitch.isChecked
                val hasWifi = binding.hasWifiSwitch.isChecked

                Log.d("AddApartmentFragment", "Submitting - Rooms: $rooms, MaxGuests: $maxGuests")

                    addApartmentViewModel.saveAdditionalDetails(
                        rooms,
                        maxGuests,
                        typeOfHome,
                        petsAllowed,
                        homeOffice,
                        hasWifi
                    )
                    findNavController().navigate(
                        AddApartmentAdditionalDetailsFragmentDirections.actionAddApartmentAdditionalDetailsFragmentToHomeFragment(
                            true
                        )
                    )
                }
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun setupTypeOfHomeDropdown() {
        val homeTypes = arrayOf("Apartment", "House", "Studio", "Villa", "Cottage")
        (binding.typeOfHomeAutoComplete as? MaterialAutoCompleteTextView)?.setSimpleItems(homeTypes)
    }

    private fun setupSliders() {
        binding.roomsSlider.addOnChangeListener { _, value, _ ->
            binding.roomsLabel.text = "Number of Rooms: ${value.toInt()}"
        }

        binding.maxGuestsSlider.addOnChangeListener { _, value, _ ->
            binding.maxGuestsLabel.text = "Max Guests: ${value.toInt()}"
        }
    }

}
