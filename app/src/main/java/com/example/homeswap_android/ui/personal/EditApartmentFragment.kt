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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentEditApartmentBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentsViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel


class EditApartmentFragment : Fragment() {

    val TAG = "ApartmentDetailsFragment"

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

        apartmentViewModel.getApartment(apartmentID)

        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            updateUI(apartment)
        }


        binding.editButton.setOnClickListener {
            toggleEditMode(true)
        }

        binding.saveChangesBTN.setOnClickListener {
            toggleEditMode(false)
        }

        binding.submitChangesBTN.setOnClickListener {
            saveChanges()
            findNavController().navigateUp()
        }

        binding.apartmentDetailsBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.changeImageFAB.setOnClickListener {
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            apartment?.let {
                Log.d("NewApartment", apartment.apartmentID)
                userID = apartment.userID
                if (selectedImageUris.isNotEmpty()) {
                    apartmentViewModel.uploadApartmentImages(selectedImageUris, it.apartmentID)
                }
            }
        }

        apartmentViewModel.deletionResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Apartment deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.myListingsFragment)
            } else {
                Toast.makeText(context, "Failed to delete apartment", Toast.LENGTH_LONG).show()
            }
        }

        binding.deleteApartmentBTN.setOnClickListener {
            showDeleteApartmentConfirmationDialog(apartmentID, userID)
        }
    }

    private fun updateUI(apartment: Apartment) {
        binding.apartmentTitleTV.text = apartment.title
        binding.countryTV.text = apartment.country
        binding.cityTV.text = apartment.city
        binding.apartmentImageIV.load(apartment.coverPicture)

        binding.apartmentTitleET.setText(apartment.title)
        binding.countryET.setText(apartment.country)
        binding.cityET.setText(apartment.city)
    }

    private fun toggleEditMode(isEditing: Boolean) {
        binding.apartmentTitleTV.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.countryTV.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.cityTV.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.editButton.visibility = if (isEditing) View.GONE else View.VISIBLE

        binding.apartmentTitleTIL.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.countryTIL.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.cityTIL.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.saveChangesBTN.visibility = if (isEditing) View.VISIBLE else View.GONE
    }

    private fun displaySelectedImages() {
        binding.selectedImagesContainer.removeAllViews()

        // Add new images
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
        val newTitle = binding.apartmentTitleET.text.toString()
        val newCountry = binding.countryET.text.toString()
        val newCity = binding.cityET.text.toString()

        apartmentViewModel.currentApartment.value?.let { apartment ->
            val updatedApartment = apartment.copy(
                title = newTitle,
                country = newCountry,
                city = newCity
            )
            apartmentViewModel.updateApartment(updatedApartment)
            if (selectedImageUris.isNotEmpty()) {
                apartmentViewModel.uploadApartmentImages(selectedImageUris, apartment.apartmentID)
            }
        }
    }

    private fun showDeleteApartmentConfirmationDialog(apartmentID: String, userID: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Apartment")
            .setMessage("Are you sure you want to delete this apartment listing? This is a permanent action and all data related to this listing will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                apartmentViewModel.deleteApartment(apartmentID, userID)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}