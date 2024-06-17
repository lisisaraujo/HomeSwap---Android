package com.example.homeswap_android.ui.login

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.createGraph
import androidx.navigation.fragment.findNavController
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentLoginBinding
import com.example.homeswap_android.viewModels.FirebaseViewModel


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: FirebaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.signOut()

        binding.loginBTN.setOnClickListener {
            val email = binding.emailET.text.toString()
            val password = binding.passwordET.text.toString()

            if (email.isBlank() || password.isBlank()) {
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = "Please enter your email and password to log in."
            } else {
                binding.errorTextView.visibility = View.GONE
                viewModel.login(email, password)

                viewModel.currentUser.observe(viewLifecycleOwner) { user ->
                    if (user == null) {
                        showToastInCenter("Invalid email or password. Please try again.")
                    } else {
                        if (!user.isEmailVerified) {
                            findNavController().navigate(R.id.verifyEmailFragment)
//                            showToastInCenter("Please verify your email to continue.")
                        } else {
                            findNavController().navigate(R.id.usersListHomeFragment)
                        }
                    }
                }
            }

        }


        binding.joinTextView.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }

    private fun showToastInCenter(message: String) {
        val toast = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }
}
