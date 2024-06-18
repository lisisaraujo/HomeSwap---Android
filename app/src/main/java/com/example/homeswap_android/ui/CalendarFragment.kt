package com.example.homeswap_android.ui

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.homeswap_android.R
import com.example.homeswap_android.databinding.FragmentCalendarBinding
import com.example.homeswap_android.viewModels.FirebaseUsersViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarFragment : Fragment() {
    private lateinit var binding: FragmentCalendarBinding
//    private val args: CalendarFragmentArgs by navArgs()

    private val viewModel: FirebaseUsersViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showDatePickerBTN.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTheme(R.style.ThemeMaterialCalendar)
                .setTitleText("Select Dates")
                .build()

            picker.show(parentFragmentManager, "calendarFragment")
            picker.addOnPositiveButtonClickListener {
                binding.selectedDate.setText(
                    convertTimeToDate(it.first) + " - " + convertTimeToDate(
                        it.second
                    )
                )
            }
            picker.addOnNegativeButtonClickListener {
                picker.dismiss()
            }
        }
    }

    private fun convertTimeToDate(time: Long): String {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = time
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return format.format(utc.time)
    }
}