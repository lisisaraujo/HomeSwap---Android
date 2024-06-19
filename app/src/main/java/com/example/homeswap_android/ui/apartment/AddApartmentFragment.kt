package com.example.homeswap_android.ui.apartment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.data.models.Apartment
import com.example.homeswap_android.databinding.FragmentAddApartmentBinding
import com.example.homeswap_android.viewModels.FirebaseApartmentViewModel

val TAG = "AddApartmentFragment"

class AddApartmentFragment : Fragment() {

    private lateinit var binding: FragmentAddApartmentBinding
    private val viewModel: FirebaseApartmentViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null

    private val getContent =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.apartmentImageIV.load(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddApartmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitApartmentBTN.setOnClickListener {
            val title = binding.titleET.text.toString()
            val country = binding.countryET.text.toString()
            val city = binding.cityET.text.toString()
            val address = binding.addressET.text.toString()

            val newApartment = Apartment(
                title = title,
                country = country,
                city = city,
                address = address,
            )

            viewModel.addApartment(newApartment)
            viewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
                if (apartment != null) {
                    selectedImageUri?.let { uri ->
                        viewModel.uploadApartmentImage(uri, apartment.apartmentID)
                    }
                    findNavController().navigate(AddApartmentFragmentDirections.actionAddApartmentFragmentToHomeFragment(true))
                }
            }
        }

        binding.apartmentImageIV.setOnClickListener {
            getContent.launch("image/*")
        }

        viewModel.currentApartment.observe(viewLifecycleOwner) { apartment ->
            Log.d(TAG, apartment.title)
        }

        viewModel.apartmentDataDocumentReference?.addSnapshotListener { value, error ->
            if (value != null) {
                Log.d("Apartment", value.data.toString())
                value.toObject(Apartment::class.java)?.let { apartment ->
                    if (apartment.pictures.isNotEmpty()) {
                        binding.apartmentImageIV.load(apartment.pictures.first()) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_foreground)
                        }
                    } else {
                        binding.apartmentImageIV.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }
            }
        }
    }
}
