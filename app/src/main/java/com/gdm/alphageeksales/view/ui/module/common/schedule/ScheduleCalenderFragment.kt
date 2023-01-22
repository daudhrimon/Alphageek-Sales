package com.gdm.alphageeksales.view.ui.module.common.schedule

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdm.alphageeksales.R
import com.gdm.alphageeksales.data.dates.DatesModel
import com.gdm.alphageeksales.databinding.FragmentScheduleCalenderBinding
import com.gdm.alphageeksales.utils.isGONE
import com.gdm.alphageeksales.utils.isVISIBLE
import com.gdm.alphageeksales.view.adapter.CustomDateAdapter
import com.gdm.alphageeksales.view.adapter.ScheduleAdapter
import com.gdm.alphageeksales.viewmodels.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ScheduleCalenderFragment : Fragment() {
    private lateinit var  binding: FragmentScheduleCalenderBinding
    private val dashboardViewModel: DashboardViewModel by viewModels()

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScheduleCalenderBinding.inflate(layoutInflater)

        setupDates()


        // get  current date schedule data
        val currentDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        dashboardViewModel.getScheduleList(currentDate.format(calendar.time))

        // observe schedule data
        dashboardViewModel.scheduleList.observe(viewLifecycleOwner) {
            if (it != null){
                // set up the information
                if (it.isNotEmpty()){
                    binding.emptyViewLayout.isGONE()
                    binding.recyclerview.isVISIBLE()
                    binding.recyclerview.layoutManager = LinearLayoutManager(requireActivity())
                    binding.recyclerview.adapter       = ScheduleAdapter(it,requireContext())
                }else{
                    binding.emptyViewLayout.isVISIBLE()
                    binding.recyclerview.isGONE()
                }
            }
        }




        //setup calender
        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener: DatePickerDialog.OnDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val myFormat = "yyyy-MM-dd"
                val sdf = SimpleDateFormat(myFormat, Locale.ENGLISH)

                dashboardViewModel.getScheduleList(sdf.format(myCalendar.time))

            }

        binding.calenderBtn.setOnClickListener{
            DatePickerDialog(
                requireActivity(),
                R.style.Calender_dialog_theme,
                dateSetListener,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }



        return binding.root
    }


    @SuppressLint("SimpleDateFormat")
    private fun setupDates() {
        val fullDate: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dateFormat: DateFormat = SimpleDateFormat("dd")
        val dayFormat: DateFormat = SimpleDateFormat("EEE")
        val calendar = Calendar.getInstance()
        val datesModelList: MutableList<DatesModel> = ArrayList()
        for (i in 0..14) {
            datesModelList.add(
                DatesModel(
                    dayFormat.format(calendar.time),
                    dateFormat.format(calendar.time),
                    fullDate.format(calendar.time)
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        binding.dateRecyclerview.layoutManager = LinearLayoutManager(
            requireActivity(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.dateRecyclerview.adapter = CustomDateAdapter(
            datesModelList,
            requireContext(),
            dashboardViewModel
        )
    }
}