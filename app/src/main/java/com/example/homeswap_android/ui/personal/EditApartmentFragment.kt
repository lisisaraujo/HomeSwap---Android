package com.example.homeswap_android.ui.apartment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        apartmentViewModel.getApartment(apartmentID)
        apartmentViewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            if (apartment != null) {
                updateUI(apartment)
            }
        }

        apartmentViewModel.getApartmentFirstPicture(apartmentID, apartmentViewModel.currentApartment.value!!.userID).observe(viewLifecycleOwner){url ->
            binding.apartmentImageIV.load(url){
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
        }


        binding.editButton.setOnClickListener {
            toggleEditMode(true)
        }

        binding.saveChangesBTN.setOnClickListener {
            saveChanges()
            toggleEditMode(false)
        }



        binding.apartmentDetailsBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.changeImageFAB.setOnClickListener {
            // launch image picker intent
        }

        binding.deleteApartmentBTN.setOnClickListener {
           showDeleteApartmentConfirmationDialog(apartmentID)
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
        }
    }

    private fun showDeleteApartmentConfirmationDialog(apartmentID: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Apartment")
            .setMessage("Are you sure you want to delete this apartment listing? This is a permanent action and all data related to this listing will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                apartmentViewModel.deleteApartment(apartmentID)
                findNavController().navigate(R.id.myListingsFragment)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}