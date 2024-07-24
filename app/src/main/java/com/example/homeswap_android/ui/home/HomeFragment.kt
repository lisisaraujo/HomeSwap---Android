package com.example.homeswap_android.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.homeswap_android.R
import com.example.homeswap_android.adapter.ViewPagerAdapter
import com.example.homeswap_android.databinding.FragmentHomeBinding
import com.example.homeswap_android.viewModels.AddApartmentViewModel
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    val TAG = "HomeFragment"

    private lateinit var binding: FragmentHomeBinding
    private val userViewModel: FirebaseUsersViewModel by activityViewModels()
    private var currentTabPosition = 0


    private val addApartmentViewModel: AddApartmentViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        userViewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            Log.d(TAG, user?.email.toString())
            if (user == null) {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragments = listOf(ApartmentsListHomeFragment(), UsersListHomeFragment())
        val adapter = ViewPagerAdapter(requireActivity().supportFragmentManager, lifecycle, fragments)


        binding.viewPager.adapter = adapter
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Apartments"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Users"))

        //restore the tab position
        currentTabPosition = savedInstanceState?.getInt("currentTabPosition") ?: 0

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position ?: 0
                binding.viewPager.currentItem = currentTabPosition
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentTabPosition = position
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(currentTabPosition))
        binding.viewPager.setCurrentItem(currentTabPosition, false)

        binding.searchBar.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentTabPosition", currentTabPosition)
    }
}
