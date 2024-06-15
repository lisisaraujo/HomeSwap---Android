package com.example.homeswap_android.ui.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
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

//        viewModel.signOut()

        binding.loginBTN.setOnClickListener {
            val email = binding.emailET.text.toString()
            val password = binding.passwordET.text.toString()

            viewModel.login(email, password)
        }

        binding.joinTextView.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                // user is not logged in
                Log.d("CurrentUser", "Kein User eingeloggt")
            } else if(!user.isEmailVerified) {
                Toast.makeText(requireContext(), "Please verify your email to continue.", Toast.LENGTH_SHORT).show()
                //User ist eingeloggt
            } else {
                Log.d("CurrentUser", user.uid)
                Log.d("Login verified", user.isEmailVerified.toString())
                findNavController().navigate(R.id.usersListHomeFragment)
            }

        }

        binding.checkFlightsBTN.setOnClickListener {
            findNavController().navigate(R.id.checkFlightsFragment)
        }

//        binding.openCalendarBTN.setOnClickListener {
//            findNavController().navigate(R.id.calendarFragment)
//        }

        binding.joinTextView.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }
}