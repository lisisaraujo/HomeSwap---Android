package com.example.homeswap_android.ui.registration

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentVerifyEmailBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel

class VerifyEmailFragment : Fragment() {
    private lateinit var binding: FragmentVerifyEmailBinding
    private val viewModel: FirebaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVerifyEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "Verification email sent", Toast.LENGTH_SHORT).show()

        binding.continueBTN.setOnClickListener {
            viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                if (user!!.isEmailVerified) findNavController().navigate(R.id.registerProfileDetailsFragment)
                else findNavController().navigate(R.id.loginFragment)
            }
        }
    }
}