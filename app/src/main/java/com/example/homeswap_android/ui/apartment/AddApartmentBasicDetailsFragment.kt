package com.example.homeswap_android.ui.apartment

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentAddApartmentBasicDetailsBinding
import com.example.homeswap_android.viewModels.AddApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale

class AddApartmentBasicDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAddApartmentBasicDetailsBinding
    private val addApartmentViewModel: AddApartmentViewModel by activityViewModels()
    private val apartmentsViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private var selectedImageUri: Uri? = null
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

    private val getContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.apartmentImageIV.load(it)
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
            val country = binding.countryET.text.toString()
            val city = binding.cityET.text.toString()
            val address = binding.addressET.text.toString()
            val startDate = selectedStartDate
            val endDate = selectedEndDate

            val newApartment = Apartment(
                title = title,
                country = country,
                countryLower = country.lowercase(),
                city = city,
                cityLower = city.lowercase(),
                address = address,
                startDate = startDate,
                endDate = endDate
            )

            addApartmentViewModel.addApartment(newApartment)
        }

        addApartmentViewModel.newAddedApartment.observe(viewLifecycleOwner) { apartment ->
            apartment?.let {
                Log.d("NewApartment", apartment.apartmentID)
                selectedImageUri?.let { uri ->
                        apartmentsViewModel.uploadApartmentImage(uri, it.apartmentID)
                }
                findNavController().navigate(R.id.addApartmentAdditionalDetailsFragment)
            }
        }


        binding.apartmentImageIV.setOnClickListener {
            getContent.launch("image/*")
        }

        binding.selectDatesBTN.setOnClickListener {
            showDateRangePicker()
        }

        binding.addApartmentBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDateRangePicker() {
        val picker =
            MaterialDatePicker.Builder.dateRangePicker().setTheme(R.style.ThemeMaterialCalendar)
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
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(utc.time)
    }
}
