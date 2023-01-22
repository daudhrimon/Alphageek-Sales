package com.gdm.alphageeksales.view.ui.module.common.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.databinding.FragmentScheduleTodayBinding
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.view.adapter.ScheduleAdapter
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ScheduleTodayFragment : Fragment() {
    private lateinit var binding: FragmentScheduleTodayBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleTodayBinding.inflate(layoutInflater)

        // get  current date schedule data
        val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        dashboardViewModel.getScheduleList(currentDate.format(calendar.time))

        // observe schedule data
        dashboardViewModel.scheduleList.observe(viewLifecycleOwner) {
            if (it != null) {
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter = ScheduleAdapter(it,requireContext())
                } else {
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }


            }
        }

        return binding.root
    }

}