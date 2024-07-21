package com.example.homeswap_android.ui.apartment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentEditApartmentBinding
import com.example.homeswap_android.utils.Utils
import com.example.homeswap_android.utils.Utils.showDateRangePicker
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel


class EditApartmentFragment : Fragment() {

    val TAG = "EditApartmentFragment"

    private lateinit var binding: FragmentEditApartmentBinding
    private val apartmentViewModel: FirebaseApartmentsViewModel by activityViewModels()
    private val args: ApartmentDetailsFragmentArgs by navArgs()
    private var selectedImageUris: List<Uri> = emptyList()
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

    // Define the photo picker launcher for multiple images
    private val pickMultipleMedia =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
                selectedImageUris = uris
                displaySelectedImages()
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditApartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apartmentID = args.apartmentID
        var userID = ""

        apartmentViewModel.getApartment(apartmentID).observe(viewLifecycleOwner) {
            updateUI(it)
        }


        binding.saveChangesBTN.setOnClickListener {
            saveChanges()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.updatePhotosBTN.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            apartment?.let {
                Log.d("CurrentApartment", apartment.apartmentID)
                userID = apartment.userID
                updateUI(apartment)
            }
        }


        binding.selectDatesBTN.setOnClickListener {
            showDateRangePicker(parentFragmentManager) { start, end ->
                selectedStartDate = start
                selectedEndDate = end
                binding.selectedDateRange.setText("$start - $end")
            }
        }


        binding.deleteApartmentBTN.setOnClickListener {
            showDeleteApartmentConfirmationDialog(apartmentID, userID)
        }
    }

    private fun updateUI(apartment: Apartment) {
        binding.apartmentTitleTV.setText(apartment.title)
        binding.updatedApartmentLocationTV.setText(apartment.city)
        binding.coverPictureIV.load(apartment.coverPicture)

    }

    private fun displaySelectedImages() {
        binding.selectedImagesContainer.removeAllViews()

        //add images
        selectedImageUris.forEach { uri ->
            val imageView = ImageView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(200, 200)
                scaleType = ImageView.ScaleType.CENTER_CROP
                load(uri)
            }
            binding.selectedImagesContainer.addView(imageView)
        }
    }

    private fun saveChanges() {
        val loadingOverlay = view?.findViewById<ConstraintLayout>(R.id.loading_overlay)

        Utils.showLoadingOverlay(loadingOverlay!!)

        val newTitle = binding.apartmentTitleTV.text.toString()
        val newCity = binding.updatedApartmentLocationTV.text.toString()
        val newDescription = binding.updatedApartmentDescriptionET.text.toString()
        val newStartDate = selectedStartDate
        val newEndDate = selectedEndDate

        Log.d(TAG, "New values - Title: $newTitle, City: $newCity")

        val currentApartment = apartmentViewModel.currentApartment.value
        if (currentApartment == null) {
            Log.e(TAG, "Current apartment is null")
            Toast.makeText(context, "Error: Unable to update apartment", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedApartment = currentApartment.copy(
            title = newTitle,
            city = newCity,
            description = newDescription,
            startDate = newStartDate,
            endDate = newEndDate,
        )
        Log.d(TAG, "Updating apartment: $updatedApartment")

        apartmentViewModel.updateApartment(
            updatedApartment
        )
        try {
            Log.d(TAG, "Apartment updated successfully")
            if (selectedImageUris.isNotEmpty()) {
                Log.d(TAG, "Uploading ${selectedImageUris.size} images")
                apartmentViewModel.uploadApartmentImages(
                    selectedImageUris,
                    currentApartment.apartmentID
                )
            } else {
                Log.d(TAG, "No new images to upload")
            }
            Utils.hideLoadingOverlay(loadingOverlay)
            Toast.makeText(context, "Changes saved successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update apartment", e)
            Toast.makeText(
                context,
                "Failed to save changes: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private fun showDeleteApartmentConfirmationDialog(apartmentID: String, userID: String) {
        val loadingOverlay = view?.findViewById<ConstraintLayout>(R.id.loading_overlay)

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Apartment")
            .setMessage("Are you sure you want to delete this apartment listing? This is a permanent action and all data related to this listing will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                Utils.showLoadingOverlay(loadingOverlay!!)
                apartmentViewModel.deleteApartment(
                    apartmentID,
                    userID,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Apartment deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        Utils.hideLoadingOverlay(loadingOverlay!!)
                        findNavController().navigateUp()
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            context,
                            "Failed to delete apartment: ${exception.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}