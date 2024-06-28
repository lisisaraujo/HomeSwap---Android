package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ViewPagerAdapter
import com.example.homeswap_android.databinding.FragmentHomeBinding
import com.example.homeswap_android.viewModels.AddApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.android.material.tabs.TabLayout

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val args: HomeFragmentArgs by navArgs()
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // check if user is logged in before going to homepage
        userViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragments = listOf(ApartmentsListHomeFragment(), UsersListHomeFragment())
        val adapter =
            ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle, fragments)


        binding.viewPager.adapter = adapter
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Apartments"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Users"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab?.position ?: 0
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        Log.d("TabChangeHome", args.isApartments.toString())

        if (args.isUsers) {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1))
            binding.viewPager.setCurrentItem(1, false)
        }

        if (args.isApartments) {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0))
            binding.viewPager.setCurrentItem(0, false)
        }

        binding.searchCV.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }


}
