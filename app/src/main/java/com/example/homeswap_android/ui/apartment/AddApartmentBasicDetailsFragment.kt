package com.example.homeswap_android.ui.apartment

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentAddApartmentBasicDetailsBinding
import com.example.homeswap_android.utils.Utils.dateFormat
import com.example.homeswap_android.viewModels.AddApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Date

class AddApartmentBasicDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAddApartmentBasicDetailsBinding
    private val addApartmentViewModel: AddApartmentViewModel by activityViewModels()
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private var selectedImageUris: List<Uri> = emptyList()
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitApartmentBTN.setOnClickListener {
            val title = binding.titleET.text.toString()
            val city = binding.cityET.text.toString()
            val address = binding.addressET.text.toString()
            val startDate = selectedStartDate
            val endDate = selectedEndDate

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

            addApartmentViewModel.addApartment(newApartment, selectedImageUris)
        }

        addApartmentViewModel.newAddedApartment.observe(viewLifecycleOwner) { newApartment ->
            newApartment?.let { apartment ->
                Log.d("NewApartment", apartment.apartmentID)
                findNavController().navigate(R.id.addApartmentAdditionalDetailsFragment)
            }
        }


        binding.selectImagesButton.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.selectDatesBTN.setOnClickListener {
            showDateRangePicker()
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

    private fun showDateRangePicker() {
        val picker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select Dates").build()

        picker.show(parentFragmentManager, "dateRangePicker")
        picker.addOnPositiveButtonClickListener { selection ->
            selectedStartDate = convertTimeToDate(selection.first)
            selectedEndDate = convertTimeToDate(selection.second)
            binding.selectedDateRange.text = "$selectedStartDate - $selectedEndDate"
        }
    }

    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        return dateFormat.format(utc.time)
    }
}