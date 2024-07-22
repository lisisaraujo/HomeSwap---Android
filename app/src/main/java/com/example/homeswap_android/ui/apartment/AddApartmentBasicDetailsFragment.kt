package com.example.homeswap_android.ui.apartment

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentAddApartmentBasicDetailsBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.utils.Utils.dateFormat
import com.example.homeswap_android.utils.Utils.hideLoadingOverlay
import com.example.homeswap_android.utils.Utils.showLoadingOverlay
import com.example.homeswap_android.viewModels.AddApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date

class AddApartmentBasicDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAddApartmentBasicDetailsBinding
    private val addApartmentViewModel: AddApartmentViewModel by activityViewModels()
    private var selectedImageUris: List<Uri> = emptyList()
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

    private lateinit var placesClient: PlacesClient


    // Define the photo picker launcher for multiple images
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(15)) { uris ->
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                selectedImageUris = uris
                displaySelectedImages()
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApartmentBasicDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingOverlay = view.findViewById<ConstraintLayout>(R.id.loading_overlay)


        placesClient = Places.createClient(requireContext())

        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.apartmentLocationET,
            placesClient
        ) { selectedLocation ->
            binding.apartmentLocationET.setText(selectedLocation)
        }

        Utils.setupAutoCompleteTextView(
            requireContext(),
            binding.addressET,
            placesClient
        ) { selectedLocation ->
            binding.addressET.setText(selectedLocation)
        }


        binding.submitApartmentBTN.setOnClickListener {
            showLoadingOverlay(loadingOverlay!!)
            val title = binding.addApartmentTitleET.text.toString()
            val city = binding.apartmentLocationET.text.toString()
            val address = binding.addressET.text.toString()
            val startDate = selectedStartDate
            val endDate = selectedEndDate

            if (title.isEmpty() || city.isEmpty() || address.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || selectedImageUris.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill out all fields and upload at least one image.", Toast.LENGTH_SHORT).show()
                hideLoadingOverlay(loadingOverlay!!)
            } else {
                val currentDate = dateFormat.format(Date())
                val newApartment = Apartment(
                    title = title,
                    city = city,
                    cityLower = city.lowercase(),
                    address = address,
                    startDate = startDate,
                    endDate = endDate,
                    registrationDate = currentDate
                )

                //observe the LiveData only after the button is clicked
                addApartmentViewModel.newAddedApartment.observe(viewLifecycleOwner) { newApartment ->
                    newApartment?.let { apartment ->
                        Log.d("NewApartment", apartment.apartmentID)
                        hideLoadingOverlay(loadingOverlay!!)
                        findNavController().navigate(R.id.addApartmentAdditionalDetailsFragment)
                        //remove the observer after navigation to prevent repeated navigation
                        addApartmentViewModel.newAddedApartment.removeObservers(viewLifecycleOwner)
                    }
                }

                //add the apartment after setting up the observer
                addApartmentViewModel.addApartment(newApartment, selectedImageUris)
            }
        }

        binding.selectImagesButton.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.selectDatesBTN.setOnClickListener {
            Utils.showDateRangePicker(parentFragmentManager) { start, end ->
                selectedStartDate = start
                selectedEndDate = end
                binding.selectedDateRange.setText("$start - $end")
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun displaySelectedImages() {
        binding.selectedImagesContainer.removeAllViews()

        selectedImageUris.forEach { uri ->
            val imageView = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(200, 200)  // Set appropriate size
                scaleType = ImageView.ScaleType.CENTER_CROP
                load(uri)
            }
            binding.selectedImagesContainer.addView(imageView)
        }
    }
}