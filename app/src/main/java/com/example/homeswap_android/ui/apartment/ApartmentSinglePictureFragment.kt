package com.example.homeswap_android.ui.apartment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentApartmentPicturesBinding
import com.example.homeswap_android.databinding.FragmentApartmentSinglePictureBinding
import com.example.homeswap_android.viewModels.ApartmentPicturesViewModel

class ApartmentSinglePictureFragment : Fragment() {
    private lateinit var binding: FragmentApartmentSinglePictureBinding
    private val args: ApartmentSinglePictureFragmentArgs by navArgs()
    private val viewModel: ApartmentPicturesViewModel by activityViewModels()

    val TAG = "ApartmentSinglePictureFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentApartmentSinglePictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadApartmentPictures(args.apartmentID, args.userID)

        viewModel.apartmentPictures.observe(viewLifecycleOwner) { pictures ->
            if (pictures.isNotEmpty()) {
                viewModel.loadSinglePicture(args.initialPosition)
            } else {
                showNoImagesMessage()
            }
        }

        viewModel.currentPicture.observe(viewLifecycleOwner) { pictureUrl ->
            if (pictureUrl != null) {
                binding.singlePictureIV.load(pictureUrl) {
                    placeholder(R.drawable.ic_launcher_foreground)
                    error(R.drawable.ic_launcher_foreground)
                }
                updateButtonVisibility()
                updateImageCounter()
            }
        }

        binding.nextButton.setOnClickListener {
            viewModel.loadNextPicture()
        }

        binding.previousButton.setOnClickListener {
            viewModel.loadPrevPicture()
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateButtonVisibility() {
        binding.previousButton.isEnabled = viewModel.picturePosition > 0
        binding.nextButton.isEnabled =
            viewModel.picturePosition < (viewModel.apartmentPictures.value?.size ?: 0) - 1
    }

    private fun updateImageCounter() {
        val totalImages = viewModel.apartmentPictures.value?.size ?: 0
        binding.imageCounterText.text =
            getString(R.string.image_counter, viewModel.picturePosition + 1, totalImages)
    }

    private fun showNoImagesMessage() {
        binding.imageCounterText.text = getString(R.string.no_images)
        binding.previousButton.isEnabled = false
        binding.nextButton.isEnabled = false
    }
}
