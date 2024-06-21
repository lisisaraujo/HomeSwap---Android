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
import com.example.homeswap_android.data.models.UserData
import com.example.homeswap_android.databinding.FragmentRegisterBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel

class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: FirebaseUsersViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.registerResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.registerProfileDetailsFragment)
            } else {
                Toast.makeText(requireContext(), "Registration failed. Email may already be in use.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.continueBTN.setOnClickListener {
            val name = binding.nameET.text.toString().trim()
            val email = binding.emailET.text.toString().trim()
            val password = binding.passwordET.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val newUserData = UserData(
                    name = name,
                    email = email,
                )
                viewModel.register(email, password, newUserData)
                viewModel.login(email, password)
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerBackBTN.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}