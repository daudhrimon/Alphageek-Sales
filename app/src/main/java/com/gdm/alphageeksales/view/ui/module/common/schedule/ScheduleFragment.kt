package com.gdm.alphageeksales.view.ui.module.common.schedule

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.databinding.FragmentScheduleBinding
import com.gdm.alphageeksales.view.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class ScheduleFragment : Fragment() {
    private lateinit var binding: FragmentScheduleBinding
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleBinding.inflate(layoutInflater)

        // setup viewpager
        val scheduleTodayFragment = ScheduleTodayFragment()
        val scheduleCalenderFragment = ScheduleCalenderFragment()

        viewPagerAdapter = ViewPagerAdapter(requireActivity())
        viewPagerAdapter.addFragment(scheduleTodayFragment,"Today")
        viewPagerAdapter.addFragment(scheduleCalenderFragment,"Calender")

        binding.outletViewPager.adapter = viewPagerAdapter
        binding.outletViewPager.currentItem = 0
        binding.outletViewPager.isUserInputEnabled = false


        TabLayoutMediator(binding.outletTabLayout, binding.outletViewPager) { tab, position ->
            tab.text = viewPagerAdapter.getTabTitle(position)
        }.attach()

        binding.createSchedule.setOnClickListener{
            findNavController().navigate(R.id.createScheduleFragment)
        }


        return binding.root
    }

}